package com.leadreach.configs;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class FlywayConfig {

    @Context
    @Singleton
    public FlywayDisabler flywayDisabler() {
        // This bean will be loaded at application startup
        System.setProperty("micronaut.flyway.enabled", "false");
        return new FlywayDisabler();
    }

    // Simple marker class
    public static class FlywayDisabler {
        public FlywayDisabler() {
            // Constructor will be called during bean creation
            System.setProperty("micronaut.flyway.enabled", "false");
        }
    }
}
