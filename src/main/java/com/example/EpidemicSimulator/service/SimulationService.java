package com.example.EpidemicSimulator.service;

import com.example.EpidemicSimulator.dto.SimulationDTO;
import com.example.EpidemicSimulator.dto.SimulationResultDTO;
import com.example.EpidemicSimulator.mapper.SimulationMapper;
import com.example.EpidemicSimulator.mapper.SimulationResultMapper;
import com.example.EpidemicSimulator.model.Simulation;
import com.example.EpidemicSimulator.repository.SimulationRepository;
import com.example.EpidemicSimulator.repository.SimulationResultRepository;
import com.example.EpidemicSimulator.service.simulation.SimulationUpdater;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final SimulationUpdater simulationUpdater;

    @Transactional
    public SimulationDTO updateSimulation(SimulationDTO simulationDTO) {
        if (simulationDTO.id() == null) throw new NullPointerException("UUID not provided");
        simulationRepository.findById(simulationDTO.id())
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        Simulation existingSimulation;
        existingSimulation = simulationMapper.toEntity(simulationDTO);
        simulationRepository.save(existingSimulation);
        simulationResultRepository.deleteAllBySimulationId(simulationDTO.id());
        List<SimulationResultDTO> results = startSimulation(simulationDTO);
        saveSimulationResults(results, existingSimulation.getId());
        return simulationDTO;
    }

    @Transactional
    public Simulation saveSimulation(SimulationDTO simulationDTO) {
        return simulationRepository.save(simulationMapper.toEntity(simulationDTO));
    }

    @Transactional
    public List<SimulationDTO> getAllSimulations() {
        return simulationRepository.findAll().stream().map(simulationMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void saveSimulationResults(List<SimulationResultDTO> results, UUID simulationId) {
        databaseService.saveSimulationResults(results, simulationId);
    }

    public List<SimulationResultDTO> createAndSaveSimulation(SimulationDTO simulationDTO) {
        Simulation simulation = saveSimulation(simulationDTO);
        List<SimulationResultDTO> results = startSimulation(simulationDTO);
        saveSimulationResults(results, simulation.getId());
        return results;
    }

    public List<SimulationResultDTO> getSimulationResults(UUID id) {
        return simulationResultRepository.findAllBySimulationId(id).orElseThrow(() -> new EntityNotFoundException("Simulation not found")).stream().map(simulationResultMapper::toDTO).collect(Collectors.toList());
    }

    public List<SimulationResultDTO> startSimulation(SimulationDTO simulation) {
        simulationUpdater.initialize(simulation);

        List<SimulationResultDTO> results = new ArrayList<>(simulation.simulationDuration());

        // Initialize first day
        results.add(simulationUpdater.initializeDayOneOfSimulation(simulation));

        // Update simulation for each day
        for (int i = 1; i < simulation.simulationDuration(); i++) {
            simulationUpdater.updateTotalInfectedPeople(simulation, i);

            // Count how many new infections occur on [i] day
            simulationUpdater.updateNewInfectionCountPerDay(i);

            // Remove infected people from healthy status on [i] day
            simulationUpdater.updateHealthyCountPerDay(i);

            // Update deceased and recovered counts
            simulationUpdater.updateDeceasedCountPerDay(simulation, i);
            simulationUpdater.updateRecoveredCountPerDay(simulation, i);

            // Calculate number of infected people
            long numberOfInfectedPeople = simulationUpdater.getNumberOfInfectedPeople(i);
            if(numberOfInfectedPeople ==0)  simulationUpdater.setInfectionCountPerDayToZero(i);


            SimulationResultDTO simulationResultDTO = new SimulationResultDTO(i, numberOfInfectedPeople,
                    simulationUpdater.getHealthyCountPerDay().get(i),
                    simulationUpdater.getDeceasedCountPerDay().get(i),
                    simulationUpdater.getRecoveredCountPerDay().get(i));
            results.add(simulationResultDTO);
        }

        return results;
    }
}
