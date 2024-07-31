package com.example.EpidemicSimulator.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
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
    private BigDecimal reproductionRate;

    @Column(name = "mortality_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal mortalityRate;

    @Column(name = "recovery_time", nullable = false)
    private Integer recoveryTime;

    @Column(name = "mortality_time", nullable = false)
    private Integer mortalityTime;

    @Column(name = "simulation_duration", nullable = false)
    private Integer simulationDuration;




}