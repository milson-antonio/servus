package com.milsondev.servus.configurations;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    /**
     * Repairs Flyway schema history (e.g., fixes checksum mismatches) before running migrations.
     * This addresses cases where a previously applied migration file was edited after being applied.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new FlywayMigrationStrategy() {
            @Override
            public void migrate(Flyway flyway) {
                // Attempt to repair first (updates checksums, removes failed migrations, etc.)
                try {
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
