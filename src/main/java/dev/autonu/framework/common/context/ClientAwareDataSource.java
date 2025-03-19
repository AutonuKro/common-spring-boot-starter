package dev.autonu.framework.common.context;

import com.zaxxer.hikari.HikariDataSource;
import dev.autonu.framework.common.model.ClientUserAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author autonu2X
 */
public class ClientAwareDataSource extends HikariDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAwareDataSource.class);

    @Override
    public Connection getConnection() throws SQLException {

        Connection connection = super.getConnection();
        return getConnection(connection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {

        Connection connection = super.getConnection(username, password);
        return getConnection(connection);
    }

    /**
     * Before doing query set {@literal current_setting} as {@literal app.current_client_id} in {@literal PostgresSQL}
     *
     * @param connection will never be {@literal null}
     */
    private static Connection getConnection(Connection connection) throws SQLException {

        try (Statement sql = connection.createStatement()) {
            ClientUserAssociation clientUserAssociation = ClientContext.get();
            Long clientId;
            if (clientUserAssociation != null) {
                clientId = clientUserAssociation.clientId();
            } else {
                clientId = -1L;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Query is being performed by client: {}", clientId);
            }
            String query = "SET app.app.current_client_id = %d";
            sql.execute(String.format(query, clientId));
        }

        return connection;
    }
}
