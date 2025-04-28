package com.leadreach.configs;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;

import javax.sql.DataSource;

@Singleton
@Requires(property = "micronaut.flyway.enabled", notEquals = "true")
public class DataSourceMigrationDisabler implements BeanCreatedEventListener<DataSource> {

    @Override
    public DataSource onCreated(BeanCreatedEvent<DataSource> event) {
        // Simply return the datasource without triggering migrations
        return event.getBean();
    }
}
