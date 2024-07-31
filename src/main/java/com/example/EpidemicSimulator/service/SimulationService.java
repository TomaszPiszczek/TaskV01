package com.example.EpidemicSimulator.service;

import com.example.EpidemicSimulator.dto.SimulationDTO;
import com.example.EpidemicSimulator.dto.SimulationResultDTO;
import com.example.EpidemicSimulator.mapper.SimulationMapper;
import com.example.EpidemicSimulator.mapper.SimulationResultMapper;
import com.example.EpidemicSimulator.model.Simulation;
import com.example.EpidemicSimulator.repository.SimulationRepository;
import com.example.EpidemicSimulator.repository.SimulationResultRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final SimulationMapper simulationMapper;
    private final SimulationResultMapper simulationResultMapper;
    private final DatabaseService databaseService;
    private final SimulationResultRepository simulationResultRepository;
    @Transactional
    public SimulationDTO updateSimulation(SimulationDTO simulationDTO) {
        if(simulationDTO.id() == null) throw new NullPointerException("UUID not provided");
        simulationRepository.findById(simulationDTO.id())
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
        Simulation existingSimulation;
        existingSimulation = simulationMapper.toEntity(simulationDTO);
        simulationRepository.save(existingSimulation);
        simulationResultRepository.deleteAllBySimulationId(simulationDTO.id());
        List<SimulationResultDTO> results = startSimulation(existingSimulation);
        saveSimulationResults(results,existingSimulation.getId());
        return simulationDTO;
    }

    @Transactional
    public Simulation saveSimulation(SimulationDTO simulationDTO) {
        return simulationRepository.save(simulationMapper.toEntity(simulationDTO));
    }
    @Transactional
    public List<SimulationDTO> getAllSimulations(){
        return simulationRepository.findAll().stream().map(simulationMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void saveSimulationResults(List<SimulationResultDTO> results, UUID simulationId) {
        databaseService.saveSimulationResults(results, simulationId);
    }

    public List<SimulationResultDTO> createAndSaveSimulation(SimulationDTO simulationDTO) {
        Simulation simulation = saveSimulation(simulationDTO);
        List<SimulationResultDTO> results = startSimulation(simulation);
        saveSimulationResults(results, simulation.getId());
        return results;
    }

    public List<SimulationResultDTO> getSimulationResults(UUID id) {
        return simulationResultRepository.findAllBySimulationId(id).orElseThrow(() -> new EntityNotFoundException("Simulation not found")).stream().map(simulationResultMapper::toDTO).collect(Collectors.toList());
    }
    private static void updateRecoveredCountPerDay(Simulation simulation, ArrayList<Long> newInfectionCountPerDay, List<Long> recoveredCountPerDay, int i) {
        if (i >= simulation.getRecoveryTime()) {
            recoveredCountPerDay.add(i, recoveredCountPerDay.get(i - 1) + newInfectionCountPerDay.get(i - simulation.getRecoveryTime()));
            newInfectionCountPerDay.set(i - simulation.getRecoveryTime(), 0L);
        } else {
            recoveredCountPerDay.add(i, 0L);
        }
    }
    private List<SimulationResultDTO> startSimulation(Simulation simulation) {
        List<SimulationResultDTO> results = new ArrayList<>(simulation.getSimulationDuration());
        ArrayList<Long> newInfectionCountPerDay = new ArrayList<>();
        List<Long> recoveredCountPerDay = new ArrayList<>();
        List<Long> deceasedCountPerDay = new ArrayList<>();
        List<Long> healthyCountPerDay = new ArrayList<>();
        List<Long> infectionCountPerDay = new ArrayList<>();
        //initialize first day
        results.add(initializeDayOneOfSimulation(simulation, newInfectionCountPerDay, recoveredCountPerDay, deceasedCountPerDay, healthyCountPerDay, infectionCountPerDay));

        //Update of simulation for each day
        for (int i = 1; i < simulation.getSimulationDuration(); i++) {


            updateTotalInfectedPeople(simulation, infectionCountPerDay, healthyCountPerDay, i);
            //Count how many new infections occur on [i] day
            newInfectionCountPerDay.add(i, infectionCountPerDay.get(i) - infectionCountPerDay.get(i - 1));
            //Remove infected people from healthy status on [i] day
            healthyCountPerDay.add(i, healthyCountPerDay.get(i - 1) - newInfectionCountPerDay.get(i));

            updateDeceasedCountPerDay(simulation, newInfectionCountPerDay, deceasedCountPerDay, i);

            updateRecoveredCountPerDay(simulation, newInfectionCountPerDay, recoveredCountPerDay, i);


            long numberOfInfectedPeople = simulation.getPopulationSize() - healthyCountPerDay.get(i) - deceasedCountPerDay.get(i) - recoveredCountPerDay.get(i);

            SimulationResultDTO simulationResultDTO = new SimulationResultDTO(i, numberOfInfectedPeople, healthyCountPerDay.get(i), deceasedCountPerDay.get(i), recoveredCountPerDay.get(i));
            results.add(simulationResultDTO);
        }
        return results;
    }

    private void updateDeceasedCountPerDay(Simulation simulation, ArrayList<Long> newInfectionCountPerDay, List<Long> deceasedCountPerDay, int day) {
        if (day >= simulation.getMortalityTime()) {
            deceasedCountPerDay.add(day, deceasedCountPerDay.get(day - 1) + calculateHowManyDeathsCurrentDay(newInfectionCountPerDay, day - simulation.getMortalityTime() + 1, simulation));
        } else {
            deceasedCountPerDay.add(day, 0L);
        }
    }

    private SimulationResultDTO initializeDayOneOfSimulation(Simulation simulation, List<Long> newInfectionCountPerDay, List<Long> recoveredCountPerDay, List<Long> deceasedCountPerDay, List<Long> healthyCountPerDay, List<Long> infectionCountPerDay) {
        newInfectionCountPerDay.add(simulation.getInitialInfectedCount());
        infectionCountPerDay.add(simulation.getInitialInfectedCount());
        recoveredCountPerDay.add(0L);
        deceasedCountPerDay.add(0L);
        healthyCountPerDay.add(0, simulation.getPopulationSize() - simulation.getInitialInfectedCount());

        return new SimulationResultDTO(0, infectionCountPerDay.get(0), healthyCountPerDay.get(0), deceasedCountPerDay.get(0), recoveredCountPerDay.get(0));
    }

    /**
     * Update list of how many infected people are on [i] day
     * number of infected people is rounded to a whole number
     * <p>
     * When reproduction rate is more than 1 value is simply multiplied
     * When reproduction rate is lower than 1 there is reproduction rate chance that new person will be infected
     */
    private void updateTotalInfectedPeople(Simulation simulation, List<Long> infectionCountPerDay, List<Long> healthyCountPerDay, int i) {

        long previousDayInfected = infectionCountPerDay.get(i - 1);
        long totalInfectionToday;
        BigDecimal reproductionRate = simulation.getReproductionRate();

        if (healthyCountPerDay.get(i - 1) == 0) {
            infectionCountPerDay.add(i, previousDayInfected);
            return;
        }
        // Generate the new infections using a binomial distribution if rate <= 1, otherwise directly use the calculated value
        if (reproductionRate.doubleValue() < 1.0) {
            BinomialDistribution newInfectionsToday = new BinomialDistribution((int) Math.min(previousDayInfected, Integer.MAX_VALUE), reproductionRate.doubleValue());
            totalInfectionToday = previousDayInfected + newInfectionsToday.sample();
        } else {
            totalInfectionToday = simulation.getReproductionRate().multiply(new BigDecimal(infectionCountPerDay.get(i - 1))).add(new BigDecimal(infectionCountPerDay.get(i - 1))).longValue();
        }
        //Check if there are still healthy people to infect
        if (healthyCountPerDay.get(i - 1) >= totalInfectionToday - previousDayInfected) {
            infectionCountPerDay.add(i, totalInfectionToday);
        } else {
            // EVERYONE IS INFECTED
            infectionCountPerDay.add(i, previousDayInfected + healthyCountPerDay.get(i - 1));
        }
    }


    /**
     * Updates list of newInfectionCountPerDay by removing dead people
     *
     * @return total number of people died  which were infected Tm days before
     */
    private int calculateHowManyDeathsCurrentDay(ArrayList<Long> newInfectionCountPerDay, int currentDay, Simulation simulation) {
        int totalDeaths = 0;
        int currentInfectedPeople;

        for (int i = 0; i < currentDay; i++) {
            long newInfections = newInfectionCountPerDay.get(i);

            if (newInfections == 0) {
                continue;
            }

            //To prevent time-consuming operation max value of infected people is Integer.MAX_VALUE otherwise using BinomialDistribution is impossible
            if (newInfectionCountPerDay.get(i) > Integer.MAX_VALUE) {
                currentInfectedPeople = Integer.MAX_VALUE;
            } else {
                currentInfectedPeople = (int) newInfections;
            }
            int deathsToday = calculateDeaths(currentInfectedPeople, simulation.getMortalityRate());

            totalDeaths += deathsToday;

            newInfectionCountPerDay.set(i, newInfectionCountPerDay.get(i) - deathsToday);
        }
        return totalDeaths;
    }

    /**
     * Used BinomialDistribution to highly increase the speed of calculation at the expense of reducing the max size of infectedCount
     *
     * @return return number of deaths for specific day
     */
    private int calculateDeaths(int infectedCount, BigDecimal mortalityRate) {
        BinomialDistribution binomial = new BinomialDistribution(infectedCount, mortalityRate.doubleValue());
        return binomial.sample();
    }



}
