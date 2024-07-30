package com.example.EpidemicSimulator.dto;


import java.math.BigDecimal;

public record SimulationDTO(
        String name,
        Long populationSize,
        Long initialInfectedCount,
        BigDecimal reproductionRate,
        BigDecimal mortalityRate,
        Integer recoveryTime,
        Integer mortalityTime,
        Integer simulationDuration
) {
}