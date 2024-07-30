package com.example.EpidemicSimulator.repository;

import com.example.EpidemicSimulator.model.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SimulationRepository extends JpaRepository<Simulation, UUID> {
}
