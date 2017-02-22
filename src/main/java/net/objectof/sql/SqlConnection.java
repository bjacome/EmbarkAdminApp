// (c) 2015 Western University (www.objectof.net)
// (c) 2015 IBM
// See License.txt in the distribution

package net.objectof.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 
 * A Sql utility containing an embedded connection.
 * 
 * @author jdh
 */
public class SqlConnection extends SqlConnector {

	final private static String WAIT_TIMEOUT = "set wait_timeout=57600";
	
	private Connection theConnection;

	public SqlConnection(Connection aConnection, String aBaseName) {
		this(aConnection, ResourceBundle.getBundle(aBaseName));
	}

	public SqlConnection(Connection aConnection, ResourceBundle aBundle) {
		this(aConnection, map(aBundle));
	}

	public SqlConnection(Connection aConnection, Map<String, String> aBundle) {
		super(aBundle);
		try (PreparedStatement stmt = aConnection.prepareStatement(WAIT_TIMEOUT)) {
			aConnection.setAutoCommit(false);		
			stmt.execute();
		} catch (SQLException e) {
			throw new SqlException(e);
		}
		theConnection = aConnection;
	}
	
	protected Connection getConnection() throws SQLException {
		return theConnection;
	}
}
