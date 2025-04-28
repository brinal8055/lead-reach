package com.leadreach.configs;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

@Singleton
@Context
@Requires(property = "micronaut.flyway.enabled", notEquals = "true")
public class DatabaseInitializer implements ApplicationEventListener<StartupEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseInitializer.class);
    
    private final DataSource dataSource;
    
    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void onApplicationEvent(StartupEvent event) {
        LOG.info("Initializing database schema manually");
        try {
            ensureDatabaseExists();
            String sql = loadSqlFromFile("/db/migration/V1__create_lead_table.sql");
            executeSql(sql);
            LOG.info("Database schema initialized successfully");
        } catch (Exception e) {
            LOG.error("Failed to initialize database schema", e);
        }
    }
    
    private void ensureDatabaseExists() throws SQLException {
        String dbName = "leadreach";
        
        try (Connection connection = dataSource.getConnection()) {
            String currentDb = connection.getCatalog();
            LOG.info("Connected to database: {}", currentDb);
            
            // If we got here, the database exists and we can connect to it
            LOG.info("Database '{}' exists and is accessible", currentDb);
            
            // Create the database if it doesn't exist
            if (!currentDb.equals(dbName)) {
                LOG.info("Creating database: {}", dbName);
                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE DATABASE IF NOT EXISTS " + dbName);
                    LOG.info("Database '{}' created successfully", dbName);
                    
                    // Use the new database
                    statement.execute("USE " + dbName);
                    LOG.info("Now using database: {}", dbName);
                }
            }
        } catch (SQLException e) {
            LOG.error("Error connecting to database", e);
            throw e;
        }
    }
    
    private String loadSqlFromFile(String resourcePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(resourcePath)))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    private void executeSql(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try {
                statement.execute(sql);
            } catch (SQLException e) {
                // Check if the error is because the table already exists
                if (e.getMessage().contains("already exists")) {
                    LOG.info("Table already exists, skipping creation");
                } else {
                    throw e;
                }
            }
        }
    }
}
