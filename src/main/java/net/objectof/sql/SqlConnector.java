// (c) 2015 Western University (www.objectof.net)
// (c) 2015 IBM
// See License.txt in the distribution

package net.objectof.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A Sql utility containing an embedded connection.
 *
 * @author jdh
 */
public abstract class SqlConnector extends Sql {
	
	private static final Logger logger = LoggerFactory.getLogger(SqlConnector.class);

	public SqlConnector(String aBaseName) {
		this(ResourceBundle.getBundle(aBaseName));
	}

	public SqlConnector(ResourceBundle aBundle) {
		this(map(aBundle));
	}

	public SqlConnector(Map<String, String> aBundle) {
		super(aBundle);
	}

	public PreparedStatement prepare(String aName) throws SQLException {
		return prepare(getConnection(), aName);
	}

	public PreparedStatement bind(String aName, Object... aArguments) throws SQLException {
		return bind(false, aName, aArguments);
	}

	public void close() {
		commit();
	}

	public void commit() {	
		try (
			Connection conn = getConnection()) {
			if (conn != null && conn.isValid(0)) conn.commit();
		} catch (SQLException e) {
			throw new SqlException(e);
		} 
	}
	
	public void rollback() {
		try (Connection conn = getConnection()){
			if (conn != null && conn.isValid(0)) conn.rollback();
		} catch (SQLException e) {
			throw new SqlException(e);
		} 
	}

	protected abstract Connection getConnection() throws SQLException;

	protected PreparedStatement bind(boolean aReturnGeneratedKeys, String aName, Object... aArguments) throws SQLException {		
		PreparedStatement statement = (aReturnGeneratedKeys ? prepareForInsert(getConnection(), aName)
				: prepare(getConnection(), aName));
		int length = aArguments.length;
		for (int i = 0; i < length; i++) {
			
				statement.setObject(i + 1, aArguments[i]);
		}
		return statement;
	}

	protected long create(String aStatement, Object... aArguments) throws SQLException {
		try (PreparedStatement stmt = bind(true, aStatement, aArguments)) {
			stmt.executeUpdate();
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				rs.next();
				long id = rs.getLong(1);
				return id;
			}
		} 
	}

	protected int execute(String aStatement, Object... aArguments) {
		try (PreparedStatement stmt = bind(aStatement, aArguments)) {
			return stmt.executeUpdate();
		} catch (SQLException e) {
			throw new SqlException(e);
		}
	}
}
