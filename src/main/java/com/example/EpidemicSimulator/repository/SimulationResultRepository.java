package com.example.EpidemicSimulator.repository;

import com.example.EpidemicSimulator.model.SimulationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SimulationResultRepository extends JpaRepository<SimulationResult, UUID> {
}
