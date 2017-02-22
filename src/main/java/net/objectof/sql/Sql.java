package net.objectof.sql;
//(c) 2015 Western University (www.objectof.net)

//(c) 2015 IBM
//See License.txt in the distribution

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL/JDBC Utility Class
 *
 * @author jdh
 *
 */
public class Sql {
	
	private static final Logger logger = LoggerFactory.getLogger(Sql.class);

	public static Map<String, String> map(ResourceBundle aBundle) {
		HashMap<String, String> map = new HashMap<>();
		for (String key : aBundle.keySet()) {
			map.put(key, aBundle.getString(key));
		}
		return map;
	}

	public final static String readFile(File aFile, String aCharset) throws SqlException {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(aFile.getAbsolutePath()));
			return new String(encoded, Charset.forName(aCharset));
		} catch (IOException e) {
			throw new SqlException(e);
		}
	}

	public final static PreparedStatement prepare(Connection aConnection, Map<String, String> aBundle,
			String aStatementName, boolean aReturnGeneratedKey) throws SQLException {
		String sql = aBundle.get(aStatementName);
		
		if (sql == null) {
			throw new SqlException("Statement '" + aStatementName + "' not defined.");
		}
		logger.debug("Statement '" + aStatementName + "':"+sql);
		return aConnection.prepareStatement(sql,
				aReturnGeneratedKey ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
	}

	public static final void executeScript(Connection aConnection, File aFile) throws SqlException {
		String ddl = readFile(aFile, "UTF8");
		executeScript(aConnection, ddl);
	}

	public static final void executeScript(Connection aConnection, String aDDL) throws SqlException {
		Statement stmt = null;
		try {
			stmt = aConnection.createStatement();
			String[] statements = aDDL.split(";\n");
			for (String statement : statements) {
				stmt.addBatch(statement);
			}
			stmt.executeBatch();
			aConnection.commit();
		} catch (SQLException e) {
			throw new SqlException(e);
		} finally {
			Sql.close(stmt);
		}
	}

	public static final void close(Connection aConnection) {
		if (aConnection == null) {
			return;
		}
		try {
			aConnection.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static final void close(ResultSet aRs) {
		if (aRs == null) {
			return;
		}
		try {
			aRs.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static final void close(Statement aStmt) {
		if (aStmt == null) {
			return;
		}
		try {
			aStmt.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private Map<String, String> theBundle;

	public Sql(String aBaseName) {
		this(ResourceBundle.getBundle(aBaseName));
	}

	public Sql(ResourceBundle aBundle) {
		theBundle = map(aBundle);
	}

	public Sql(Map<String, String> aBundle) {
		theBundle = aBundle;
	}

	/**
	 * Prepare with generated keys being returned.
	 *
	 * @param aConnection
	 * @param aName
	 * @return
	 */
	public PreparedStatement prepareForInsert(Connection aConnection, String aName) {
		PreparedStatement stmt = null;
		try {
			stmt = prepare(aConnection, theBundle, aName, true);
		} catch (SQLException e) {
			close(stmt);
			throw new SqlException(e);
		}
		return stmt;
	}

	public PreparedStatement prepare(Connection aConnection, String aName) {
		PreparedStatement stmt = null;
		try {
			stmt = prepare(aConnection, theBundle, aName, false);
		} catch (SQLException e) {
			close(stmt);
			throw new SqlException(e);
		}
		return stmt;
	}

	protected Map<String, String> getStatements() {
		return theBundle;
	}
}
