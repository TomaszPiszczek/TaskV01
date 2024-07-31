package com.example.EpidemicSimulator.mapper;

import com.example.EpidemicSimulator.dto.SimulationResultDTO;
import com.example.EpidemicSimulator.model.SimulationResult;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SimulationResultMapper {

    private final ModelMapper modelMapper;
    public SimulationResultDTO toDTO(SimulationResult simulationResult) {

        return new SimulationResultDTO(
                simulationResult.getDay(),
                simulationResult.getInfectedCount(),
                simulationResult.getHealthyCount(),
                simulationResult.getDeceasedCount(),
                simulationResult.getRecoveredCount());
    }

    public SimulationResult toEntity(SimulationResultDTO simulationResultDTO) {
        return modelMapper.map(simulationResultDTO, SimulationResult.class);
    }
}