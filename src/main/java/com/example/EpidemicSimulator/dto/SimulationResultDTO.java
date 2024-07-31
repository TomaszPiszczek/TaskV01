package com.example.EpidemicSimulator.dto;



import jakarta.validation.constraints.NotNull;


public record SimulationResultDTO(

        @NotNull(message = "Day cannot be null")
        Integer day,

        @NotNull(message = "Infected count cannot be null")
        Long infectedCount,

        @NotNull(message = "Healthy count cannot be null")
        Long healthyCount,

        @NotNull(message = "Deceased count cannot be null")
        Long deceasedCount,

        @NotNull(message = "Recovered count cannot be null")
        Long recoveredCount
) {
}