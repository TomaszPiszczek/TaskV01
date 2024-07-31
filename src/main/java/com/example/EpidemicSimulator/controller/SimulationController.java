package com.example.EpidemicSimulator.controller;

import com.example.EpidemicSimulator.dto.SimulationDTO;
import com.example.EpidemicSimulator.dto.SimulationResultDTO;
import com.example.EpidemicSimulator.service.SimulationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/v1/simulations")
public class SimulationController {

    private final SimulationService simulationService;


    @PostMapping()
    ResponseEntity<List<SimulationResultDTO>> createAndSaveSimulation(@Valid @RequestBody SimulationDTO simulationDTO){

        if(simulationDTO!=null){
            return  ResponseEntity.ok(simulationService.createAndSaveSimulation(simulationDTO));
        }else {
            throw new NullPointerException("Error creating simulation. Simulation is null");
        }
    }

    @GetMapping("/results/{SimulationUUID}")
    ResponseEntity<List<SimulationResultDTO>> simulationResults(@PathVariable UUID SimulationUUID){
        return ResponseEntity.ok(simulationService.getSimulationResults(SimulationUUID));
    }

    @GetMapping("/")
    ResponseEntity<List<SimulationDTO>> simulations(){
        return ResponseEntity.ok(simulationService.getAllSimulations());
    }


    @PutMapping()
    ResponseEntity<SimulationDTO> updateSimulation(
            @Valid @RequestBody SimulationDTO simulationDTO) {

        if (simulationDTO != null) {
            SimulationDTO updatedSimulation = simulationService.updateSimulation(simulationDTO);
            return ResponseEntity.ok(updatedSimulation);
        } else {
            throw new NullPointerException("Error updating simulation. Simulation is null");
        }
    }





}
