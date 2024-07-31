package com.example.EpidemicSimulator.repository;

import com.example.EpidemicSimulator.model.SimulationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimulationResultRepository extends JpaRepository<SimulationResult, UUID> {

    @Query("SELECT sr FROM SimulationResult sr WHERE sr.simulation.id = :simulationId")
    Optional<List<SimulationResult>> findAllBySimulationId(@Param("simulationId") UUID simulationId);

    @Modifying
    @Query("DELETE FROM SimulationResult sr WHERE sr.simulation.id = :simulationId")
    void deleteAllBySimulationId(@Param("simulationId") UUID simulationId);

}
