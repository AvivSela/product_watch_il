package com.avivse.retailfileservice.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                        .withDetail("database", "Available")
                        .withDetail("validationQuery", "Connection valid")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "Connection invalid")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}