package com.example.EpidemicSimulator.mapper;

import com.example.EpidemicSimulator.dto.SimulationDTO;
import com.example.EpidemicSimulator.model.Simulation;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
@AllArgsConstructor
@Component
public class SimulationMapper {
    private final ModelMapper modelMapper;

    public SimulationDTO toDTO(Simulation simulation) {
        return new SimulationDTO(
                simulation.getId(),
                simulation.getName(),
                simulation.getPopulationSize(),
                simulation.getInitialInfectedCount(),
                simulation.getReproductionRate(),
                simulation.getMortalityRate(),
                simulation.getRecoveryTime(),
                simulation.getMortalityTime(),
                simulation.getSimulationDuration());
    }

    public Simulation toEntity(SimulationDTO simulationDTO) {
        return modelMapper.map(simulationDTO, Simulation.class);
    }
}