package com.leadreach;

import io.micronaut.runtime.Micronaut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    
    public static void main(String[] args) {
        LOG.info("Starting Lead Service application");
        try {
            Micronaut.run(Application.class, args);
            LOG.info("Lead Service started successfully");
        } catch (Exception e) {
            LOG.error("Failed to start Lead Service", e);
            throw e;
        }
    }
}
