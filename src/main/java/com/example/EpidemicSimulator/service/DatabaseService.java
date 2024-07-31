package com.example.EpidemicSimulator.service;

import com.example.EpidemicSimulator.dto.SimulationResultDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DatabaseService {

    @Value("${database.url}")
    private String DB_URL;

    @Value("${database.username}")
    private String DB_USERNAME;

    @Value("${database.password}")
    private String DB_PASSWORD;
    @Transactional
    public void saveSimulationResults(List<SimulationResultDTO> results,UUID simulationId) {

        String sqlValues =  formatSqlValues(results,simulationId);

        String INSERT_SQL = "INSERT INTO simulation_results (day_of_simulation, deceased_count, healthy_count, infected_count, recovered_count, simulation_id, simulation_result_id) VALUES ";

        String sql = INSERT_SQL + sqlValues;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String formatSqlValues(List<SimulationResultDTO> results, UUID simulationId) {
        StringBuilder sqlBuilder = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            SimulationResultDTO result = results.get(i);
            sqlBuilder.append(String.format("(%d, %d, %d, %d, %d, '%s','%s')",
                    result.day(),
                    result.deceasedCount(),
                    result.healthyCount(),
                    result.infectedCount(),
                    result.recoveredCount(),
                    simulationId,
                    UUID.randomUUID()));
            if (i < results.size() - 1) {
                sqlBuilder.append(", ");
            } else {
                sqlBuilder.append(";");
            }
        }
        return sqlBuilder.toString();
    }
}