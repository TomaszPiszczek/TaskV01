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
import org.springframework.stereotype.Service;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final SimulationResultRepository simulationResultRepository;
    private final SimulationMapper simulationMapper;
    private final SimulationResultMapper simulationResultMapper;
    @Transactional
    public void createSimulation(SimulationDTO simulationDTO) {
        simulationRepository.save(simulationMapper.toEntity(simulationDTO));
    }
    @Transactional
    public void createSimulationResult(SimulationResultDTO simulationResultDTO){
        simulationResultRepository.save(simulationResultMapper.toEntity(simulationResultDTO));
    }

    public SimulationResultDTO getSimulationResults(UUID simulationUUID) {
        Simulation simulation = simulationRepository.findById(simulationUUID)
                .orElseThrow(() -> new EntityNotFoundException("Simulation with provided UUID doesn't exist"));


        SimulationResultDTO simulationResultDTO =   startSimulation(simulation);

        return simulationResultDTO;
    }

    private SimulationResultDTO startSimulation(Simulation simulation)
    {
        //Represent how many new infection occur in [i] day
        ArrayList<Long> newInfectionCountPerDay = new ArrayList<>();
        newInfectionCountPerDay.add(simulation.getInitialInfectedCount());

        ArrayList<Long> infectionCountPerDay = new ArrayList<>();
        infectionCountPerDay.add(simulation.getInitialInfectedCount());

        List<Long> recoveredCountPerDay = new ArrayList<>();
        recoveredCountPerDay.add(0L);
        List<Long> deceasedCountPerDay = new ArrayList<>();
        deceasedCountPerDay.add(0L);
        List<Long> healthyCountPerDay = new ArrayList<>();
        healthyCountPerDay.add(0,simulation.getPopulationSize()-simulation.getInitialInfectedCount());
        SimulationResultDTO simulationResultDTO = new SimulationResultDTO(simulation,0,infectionCountPerDay.get(0),healthyCountPerDay.get(0),deceasedCountPerDay.get(0),recoveredCountPerDay.get(0));
        simulationResultRepository.save(simulationResultMapper.toEntity(simulationResultDTO));

        for (int i = 1; i < simulation.getSimulationDuration(); i++) {
            //Count how many infected people is on [i] day  ( infectedCountInPreviousDay * reproductionRate  + infectedCountInPreviousDay )
            long estimatedInfectionToday = simulation.getReproductionRate().multiply(new BigDecimal(infectionCountPerDay.get(i-1))).add(new BigDecimal(infectionCountPerDay.get(i-1))).longValueExact();
            if(healthyCountPerDay.get(i-1) >= estimatedInfectionToday){
                infectionCountPerDay.add(i,estimatedInfectionToday);
            }else {
                //EVERYONE IS INFECTED
                infectionCountPerDay.add(i,infectionCountPerDay.get(i-1) + healthyCountPerDay.get(i-1));
            }

            //Count how many new infections occur on [i] day
            newInfectionCountPerDay.add(i,infectionCountPerDay.get(i) - infectionCountPerDay.get(i-1));
            //Remove infected people from healthy status on [i] day
            healthyCountPerDay.add(i,healthyCountPerDay.get(i-1) - newInfectionCountPerDay.get(i));

        }

        ArrayList<Long> deathsPerDay = calculateNumberOfDeathsForEachDay(newInfectionCountPerDay,simulation);

        for (int i = 1; i < simulation.getSimulationDuration(); i++) {

            if(i >= simulation.getMortalityTime()){
                deceasedCountPerDay.add(i,deceasedCountPerDay.get(i-1) + calculateHowManyDeathsCurrentDay(newInfectionCountPerDay,deathsPerDay,i - simulation.getMortalityTime() + 1) );
                newInfectionCountPerDay = killPeopleWhichAreInfected(newInfectionCountPerDay,deathsPerDay,i - simulation.getMortalityTime() +1);
            }else {
                deceasedCountPerDay.add(i,0L);
            }

            if(i >= simulation.getRecoveryTime()){
                recoveredCountPerDay.add(i ,recoveredCountPerDay.get(i-1) + newInfectionCountPerDay.get(i - simulation.getRecoveryTime()));
                newInfectionCountPerDay.set(i - simulation.getRecoveryTime(),0L);
            }else {
                recoveredCountPerDay.add(i,0L);
            }



        }

        for (int i = 1; i < simulation.getSimulationDuration(); i++) {


            infectionCountPerDay.set(i,infectionCountPerDay.get(i) -( recoveredCountPerDay.get(i) + deceasedCountPerDay.get(i)));

            simulationResultDTO = new SimulationResultDTO(simulation,i,infectionCountPerDay.get(i),healthyCountPerDay.get(i),deceasedCountPerDay.get(i),recoveredCountPerDay.get(i));

            simulationResultRepository.save(simulationResultMapper.toEntity(simulationResultDTO));
        }


        return null;
    }
    private Long calculateHowManyDeathsCurrentDay(ArrayList<Long> newInfectionCountPerDay, ArrayList<Long> deathsPerDay, int currentDay){
        long totalDeaths = 0;
        for (int i = 0; i < currentDay; i++) {
            if(newInfectionCountPerDay.get(i) >= deathsPerDay.get(i)){
                totalDeaths +=  deathsPerDay.get(i);
            }else {
                totalDeaths +=newInfectionCountPerDay.get(i);
            }
        }
        return totalDeaths;

    }



    private ArrayList<Long> killPeopleWhichAreInfected(ArrayList<Long> newInfectionCountPerDay, ArrayList<Long> deathsPerDay, int currentDay) {
        for (int i = 0; i < currentDay; i++) {

            if(newInfectionCountPerDay.get(i) >= deathsPerDay.get(i)){
                newInfectionCountPerDay.set(i,newInfectionCountPerDay.get(i) - deathsPerDay.get(i));
            }else {
                //All people infected in day [i] - simulation time to death ARE KILLED
                newInfectionCountPerDay.set(i,0L);
            }

        }
        return newInfectionCountPerDay;
    }

    /**
     *
     * Calculate how many people die each day depending on mortality ratio
     * result is always lowered because can't be half person dead
     */
     private ArrayList<Long> calculateNumberOfDeathsForEachDay(List<Long> newInfectionCountPerDay,Simulation simulationSettings){
         ArrayList<Long> deathsPerDay = new ArrayList<>();
         for (int i = 0; i < newInfectionCountPerDay.size(); i++) {
             BigDecimal infectionCount = new BigDecimal(newInfectionCountPerDay.get(i));
             BigDecimal mortalityValue = simulationSettings.getMortalityRate().multiply(infectionCount);
             BigDecimal flooredMortalityValue = mortalityValue.setScale(0, RoundingMode.HALF_UP);
             long mortalityLong = flooredMortalityValue.longValueExact();

             deathsPerDay.add(i, mortalityLong);
         }



        return deathsPerDay;
    }

    /*
    private SimulationResultDTO updateDeceasedCount(SimulationResultDTO currentSimulationStatus,Simulation simulationSettings){
        return null;
    }
    private SimulationResultDTO updateRecoveredCount(SimulationResultDTO currentSimulationStatus,Simulation simulationSettings ){
        return null;
    }*/
}
