package com.milsondev.servus.configurations;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new FlywayMigrationStrategy() {
            @Override
            public void migrate(Flyway flyway) {
                try {
                    //flyway.clean();
                    //flyway.migrate();
                    flyway.repair();
                } catch (Exception ignored) {
                    // If repair fails for any reason, proceed to migrate and let Flyway report issues.
                }
                // Then proceed with standard migration
                flyway.migrate();
            }
        };
    }
}
