package com.leadreach.config;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.sql.DataSource;
import java.util.List;

@Factory
public class JdbiFactory {

    @Inject
    private DataSource dataSource;

    @Singleton
    @Requires(beans = DataSource.class)
    public Jdbi jdbi() {
        Jdbi jdbi = Jdbi.create(dataSource);
        
        // Register plugins
        jdbi.installPlugin(new SqlObjectPlugin());
        
        return jdbi;
    }
}
