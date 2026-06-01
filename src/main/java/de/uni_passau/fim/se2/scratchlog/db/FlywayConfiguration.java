/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.db;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Java migrations for Flyway.
 *
 * <p>
 * Note: we cannot place the migrations in package {@code db.migrations} since
 * this is out of scope for the Spring injection mechanism. Therefore, we
 * inject all the migrations here and tell Flyway manually to use them.
 * <p>
 * For this to work, all migrations need to be annotated as {@code @Component}.
 */
@Configuration
public class FlywayConfiguration implements FlywayConfigurationCustomizer {

    /**
     * Our Java database migrations.
     */
    private final JavaMigration[] migrations;

    /**
     * Autowiring constructor.
     *
     * @param migrations Our database migrations.
     */
    @Autowired
    public FlywayConfiguration(final JavaMigration[] migrations) {
        this.migrations = migrations;
    }

    /**
     * Adds our database migrations to the Flyway configuration.
     *
     * @param configuration The Flyway configuration.
     */
    @Override
    public void customize(final FluentConfiguration configuration) {
        configuration.javaMigrations(migrations);
    }

}
