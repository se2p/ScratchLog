/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package db.migration;

import fim.unipassau.de.scratchLog.util.ApplicationProperties;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Class to performing an update on the experiment table, adding a column for specifying a Scratch GUI-URL.
 */
//CHECKSTYLE:OFF
public class V2__add_url_column extends BaseJavaMigration {

//CHECKSTYLE:ON

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
        stmt.setString(1, ApplicationProperties.GUI_URL[0]);
        stmt.executeUpdate();
        stmt.close();
    }

}
