package db.migration;

import fim.unipassau.de.scratch1984.util.ApplicationProperties;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Class to performing an update on the experiment table, adding a column for specifying a Scratch GUI-URL.
 */
@SuppressWarnings("checkstyle:TypeName")
public class V2__add_url_column extends BaseJavaMigration {

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
        st.execute("ALTER TABLE scratch1984.experiment ADD gui_url varchar(2000) NULL;");
        st.close();
        PreparedStatement stmt = connection.prepareStatement("UPDATE experiment SET gui_url = ?;");
        stmt.setString(1, ApplicationProperties.GUI_URL[0]);
        stmt.executeUpdate();
        stmt.close();
    }

}
