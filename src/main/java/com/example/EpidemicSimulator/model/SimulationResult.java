package com.example.EpidemicSimulator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Entity
@Setter
@Getter
@Table(name = "simulation_results")
public class SimulationResult {

    @Id
    @Column(name = "simulation_result_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @Column(name = "day_of_simulation",nullable = false)
    private Integer day;

    @Column(name = "infected_count", nullable = false)
    private Long infectedCount;

    @Column(name = "healthy_count", nullable = false)
    private Long healthyCount;

    @Column(name = "deceased_count", nullable = false)
    private Long deceasedCount;

    @Column(name = "recovered_count", nullable = false)
    private Long recoveredCount;
}
