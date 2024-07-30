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
        return modelMapper.map(simulation, SimulationDTO.class);
    }

    public Simulation toEntity(SimulationDTO simulationDTO) {
        Simulation simulation =  modelMapper.map(simulationDTO, Simulation.class);
        return  simulation;
    }
}