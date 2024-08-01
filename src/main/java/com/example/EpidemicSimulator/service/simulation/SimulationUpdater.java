package com.example.EpidemicSimulator.service.simulation;

import com.example.EpidemicSimulator.dto.SimulationDTO;
import com.example.EpidemicSimulator.dto.SimulationResultDTO;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class SimulationUpdater {
    private List<Long> newInfectionCountPerDay;
    private List<Long> recoveredCountPerDay;
    private List<Long> deceasedCountPerDay;
    private List<Long> healthyCountPerDay;
    private List<Long> infectionCountPerDay;
    private long populationSize;

    public void initialize(SimulationDTO simulation) {
        this.newInfectionCountPerDay = new ArrayList<>(simulation.simulationDuration());
        this.recoveredCountPerDay = new ArrayList<>(simulation.simulationDuration());
        this.deceasedCountPerDay = new ArrayList<>(simulation.simulationDuration());
        this.healthyCountPerDay = new ArrayList<>(simulation.simulationDuration());
        this.infectionCountPerDay = new ArrayList<>(simulation.simulationDuration());
        this.populationSize = simulation.populationSize();
    }

    public void updateTotalInfectedPeople(SimulationDTO simulation, int i) {
        long previousDayInfected = infectionCountPerDay.get(i - 1);
        if(previousDayInfected == 0) {
            infectionCountPerDay.add(0L);
            return;
        }
        long totalInfectionToday;
        BigDecimal reproductionRate = simulation.reproductionRate();

        if (healthyCountPerDay.get(i - 1) == 0) {
            infectionCountPerDay.add(i, previousDayInfected);
            return;
        }
        if (reproductionRate.doubleValue() < 1.0) {
            BinomialDistribution newInfectionsToday = new BinomialDistribution((int) Math.min(previousDayInfected, Integer.MAX_VALUE), reproductionRate.doubleValue());
            totalInfectionToday = previousDayInfected + newInfectionsToday.sample();
        } else {
            totalInfectionToday = simulation.reproductionRate().multiply(new BigDecimal(infectionCountPerDay.get(i - 1))).add(new BigDecimal(infectionCountPerDay.get(i - 1))).longValue();
        }

        if (healthyCountPerDay.get(i - 1) >= totalInfectionToday - previousDayInfected) {
            infectionCountPerDay.add(i, totalInfectionToday);
        } else {
            infectionCountPerDay.add(i, previousDayInfected + healthyCountPerDay.get(i - 1));
        }

    }

    public void updateRecoveredCountPerDay(SimulationDTO simulation, int i) {
        if (i >= simulation.recoveryTime()) {
            recoveredCountPerDay.add(i, recoveredCountPerDay.get(i - 1) + newInfectionCountPerDay.get(i - simulation.recoveryTime()));

            newInfectionCountPerDay.set(i - simulation.recoveryTime(), 0L);

        } else {
            recoveredCountPerDay.add(i, 0L);
        }
    }

    public void updateDeceasedCountPerDay(SimulationDTO simulation, int day) {
        if (day >= simulation.mortalityTime()) {
            deceasedCountPerDay.add(day, deceasedCountPerDay.get(day - 1) + calculateHowManyDeathsCurrentDay(day - simulation.mortalityTime() + 1, simulation.mortalityRate()));
        } else {
            deceasedCountPerDay.add(day, 0L);
        }
    }
    public void updateHealthyCountPerDay(int i) {
        long newInfections = infectionCountPerDay.get(i) - infectionCountPerDay.get(i - 1);
        healthyCountPerDay.add(i, healthyCountPerDay.get(i - 1) - newInfections);
    }

    public void updateNewInfectionCountPerDay(int i) {
        long newInfections = infectionCountPerDay.get(i) - infectionCountPerDay.get(i - 1);
        newInfectionCountPerDay.add(i, newInfections);
    }


    private int calculateHowManyDeathsCurrentDay(int currentDay, BigDecimal mortalityRate) {
        int totalDeaths = 0;
        int currentInfectedPeople;

        for (int i = 0; i < currentDay; i++) {
            long newInfections = newInfectionCountPerDay.get(i);

            if (newInfections == 0) {
                continue;
            }

            if (newInfectionCountPerDay.get(i) > Integer.MAX_VALUE) {
                currentInfectedPeople = Integer.MAX_VALUE;
            } else {
                currentInfectedPeople = (int) newInfections;
            }
            int deathsToday = calculateDeaths(currentInfectedPeople, mortalityRate);

            totalDeaths += deathsToday;

            newInfectionCountPerDay.set(i, newInfectionCountPerDay.get(i) - deathsToday);
        }
        return totalDeaths;
    }

    private int calculateDeaths(int infectedCount, BigDecimal mortalityRate) {
        BinomialDistribution binomial = new BinomialDistribution(infectedCount, mortalityRate.doubleValue());
        return binomial.sample();
    }

    public SimulationResultDTO initializeDayOneOfSimulation(SimulationDTO simulation) {
        newInfectionCountPerDay.add(simulation.initialInfectedCount());
        infectionCountPerDay.add(simulation.initialInfectedCount());
        recoveredCountPerDay.add(0L);
        deceasedCountPerDay.add(0L);
        healthyCountPerDay.add(0, simulation.populationSize() - simulation.initialInfectedCount());

        return new SimulationResultDTO(0, infectionCountPerDay.get(0), healthyCountPerDay.get(0), deceasedCountPerDay.get(0), recoveredCountPerDay.get(0));
    }


    public List<Long> getRecoveredCountPerDay() {
        return recoveredCountPerDay;
    }

    public List<Long> getDeceasedCountPerDay() {
        return deceasedCountPerDay;
    }

    public List<Long> getHealthyCountPerDay() {
        return healthyCountPerDay;
    }

    public void setInfectionCountPerDayToZero(int day) {
        this.infectionCountPerDay.set(day,0L);
    }

    public List<Long> getNewInfectionCountPerDay() {
        return newInfectionCountPerDay;
    }

    public List<Long> getInfectionCountPerDay() {
        return infectionCountPerDay;
    }

    public long getPopulationSize() {
        return populationSize;
    }

    public long getNumberOfInfectedPeople(int i) {

        return  populationSize
                - healthyCountPerDay.get(i)
                - deceasedCountPerDay.get(i)
                - recoveredCountPerDay.get(i);
    }
}
