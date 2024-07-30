package com.example.EpidemicSimulator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "simulation")
public class Simulation {

    @Id
    @Column(name = "simulation_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "simulation_name",nullable = false)
    private String name;

    @Column(name = "population_size", nullable = false)
    private Long populationSize;

    @Column(name = "initial_infected_count", nullable = false)
    private Long initialInfectedCount;

    @Column(name = "reproduction_rate", nullable = false, precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Reproduction rate must be between 0.00% and 100.00%")
    @DecimalMax(value = "100.00", message = "Reproduction rate must be between 0.00% and 100.00%")
    private BigDecimal reproductionRate;

    @Column(name = "mortality_rate", nullable = false, precision = 5, scale = 2)
    @DecimalMin(value = "0.00",  message = "Mortality rate must be between 0.00% and 100.00%")
    @DecimalMax(value = "100.00",  message = "Mortality rate must be between 0.00% and 100.00%")
    private BigDecimal mortalityRate;

    @Column(name = "recovery_time", nullable = false)
    private Integer recoveryTime;

    @Column(name = "mortality_time", nullable = false)
    private Integer mortalityTime;

    @Column(name = "simulation_duration", nullable = false)
    private Integer simulationDuration;

    public void setReproductionRate(BigDecimal reproductionRate) {
        if (reproductionRate.compareTo(BigDecimal.ZERO) < 0 || reproductionRate.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Reproduction rate must be between 0.00% and 100.00%");
        }
        this.reproductionRate = reproductionRate;
    }
    public void setMortalityRate(BigDecimal mortalityRate) {
        if (mortalityRate.compareTo(BigDecimal.ZERO) < 0 || mortalityRate.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Mortality rate must be between 0.00% and 100.00%");
        }
        this.mortalityRate = mortalityRate;
    }


}