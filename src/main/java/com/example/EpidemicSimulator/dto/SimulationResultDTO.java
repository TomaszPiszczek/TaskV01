package com.example.EpidemicSimulator.dto;


import com.example.EpidemicSimulator.model.Simulation;

public record SimulationResultDTO(
        Simulation simulation,
        Integer day,
        Long infectedCount,
        Long healthyCount,
        Long deceasedCount,
        Long recoveredCount
) {
}