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

package de.uni_passau.fim.se2.scratchlog.db.migration;

import de.uni_passau.fim.se2.scratchlog.util.ApplicationProperties;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Class to performing an update on the experiment table, adding a column for specifying a Scratch GUI-URL.
 */
//CHECKSTYLE:OFF
@Component
public class V2__add_url_column extends BaseJavaMigration {

//CHECKSTYLE:ON

    /**
     * The application properties. Contains the Scratch UI URL.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * Default constructor for autowiring.
     *
     * @param applicationProperties The application properties.
     */
    @Autowired
    public V2__add_url_column(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Adds a new column for the URL to the instrumented Scratch-GUI to the experiment table. For any existing
     * experiment, the URL is set to the current value specified in the GUI_URL constant.
     *
     * @param context The context used by flyway to establish a database connection and execute the statements.
     * @throws Exception If the database update could not be completed successfully.
     */
    @Override
    public void migrate(final Context context) throws Exception {
        Connection connection = context.getConnection();
        Statement st = connection.createStatement();
        st.execute("ALTER TABLE experiment ADD gui_url varchar(2000) NULL;");
        st.close();
        PreparedStatement stmt = connection.prepareStatement("UPDATE experiment SET gui_url = ?;");
        stmt.setString(1, applicationProperties.getScratchGuiBaseUrls()[0]);
        stmt.executeUpdate();
        stmt.close();
    }

}
