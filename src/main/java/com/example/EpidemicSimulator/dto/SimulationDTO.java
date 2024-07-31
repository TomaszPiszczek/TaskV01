package com.example.EpidemicSimulator.dto;

import jakarta.validation.constraints.*;


import java.math.BigDecimal;
import java.util.UUID;

public record SimulationDTO(
                            UUID id,

                            @NotBlank(message = "Name must not be blank") String name,
                            @NotNull(message = "Population size must not be null") @Min(value = 1 , message = "Population must be bigger than 0") Long populationSize,
                            @NotNull(message = "Initial infected count must not be null") @Min(value = 1 , message = "Infected count must be bigger than 0") Long initialInfectedCount,
                            @DecimalMin(value = "0.00", message = "Reproduction rate must be higher than 0") BigDecimal reproductionRate,

                            @Digits(integer = 3, fraction = 2,message ="Mortality rate have  two decimal places" ) @DecimalMin(value = "0.00", message = "Mortality rate must be between 0 and 1 with with a maximum of two decimal numbers" )  @DecimalMax( value = "1.00", message = "Mortality rate must be between 0 and 1 with a maximum of two decimal numbers") BigDecimal mortalityRate,
                            @NotNull(message = "recovery time must not be null") Integer recoveryTime,
                            @NotNull(message = "mortality time must not be null") Integer mortalityTime,
                            @NotNull(message = "simulation duration must not be null") Integer simulationDuration) {

}
