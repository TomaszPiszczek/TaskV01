package com.example.EpidemicSimulator.controller;

import com.example.EpidemicSimulator.dto.SimulationDTO;
import com.example.EpidemicSimulator.dto.SimulationResultDTO;
import com.example.EpidemicSimulator.service.SimulationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/simulations")
public class SimulationController {

    SimulationService simulationService;

    @PostMapping()
    ResponseEntity<SimulationDTO> createSimulation(@RequestBody @Valid SimulationDTO simulationDTO){
        if(simulationDTO!=null){

            simulationService.createSimulation(simulationDTO);
        }else {
            throw new NullPointerException("Error creating simulation. Simulation is null");
        }

        return ResponseEntity
                .created(URI.create("/api/simulations/" + simulationDTO.name().replaceAll("\\s", "")))
                .body(simulationDTO);
    }


    @GetMapping("/{simulationUUID}")
    ResponseEntity<SimulationResultDTO> getSimulationResults(@PathVariable UUID simulationUUID){
        return  ResponseEntity.ok(simulationService.getSimulationResults(simulationUUID));
    }
}
