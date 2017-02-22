package com.rbc.b2e.embark.admin.connection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.rbc.b2e.embark.admin.util.SystemConstants;

public class MysqlDatasourceProvider extends GenericDataSourceProvider implements DataSourceProvider{

	private static final Logger logger = LoggerFactory
			.getLogger(MysqlDatasourceProvider.class);

	final private static String NOW_SQL = "SELECT NOW()";

	final private MysqlDataSource mysqlDataSource = new MysqlDataSource();

	public MysqlDatasourceProvider(String serviceName, String vcapServices) {
		this.buildMysqlCredential(serviceName, vcapServices);
		testDatabaseConnection();

	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return this.mysqlDataSource.getConnection();
	}
	@Override
	public MysqlDataSource getDataSource() {
		return this.mysqlDataSource;
	}

	private void testDatabaseConnection() {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(NOW_SQL);
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			if (rs.next()) {
				Timestamp date = rs.getTimestamp(1);
				logger.debug("MysqlConnectionProvider up and ready at: "+ simpleDateFormat.format(date));
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(),e);
		} finally {
			closeStatement(st);
			closeResultSet(rs);
			closeConnection(conn);
		}
	}

	private void buildMysqlCredential(String serviceName, String vcapServices) {
		logger.debug("vcapServices is " + vcapServices);
		JsonObject obj = (JsonObject) new JsonParser().parse(vcapServices);
		Entry<String, JsonElement> dbEntry = null;
		Set<Entry<String, JsonElement>> entries = obj.entrySet();
		// Look for the VCAP key that holds the sql db
		// information
		for (Entry<String, JsonElement> eachEntry : entries) {
			if (eachEntry.getKey().equals(serviceName)) {
				dbEntry = eachEntry;
				break;
			}
		}
		if (dbEntry == null) {
			logger.error("MysqlDatasourceProvider: ",
					"Could not find "+serviceName+" key in VCAP_SERVICES env variable");
			throw new RuntimeException(
					"Could not find "+serviceName+" key in VCAP_SERVICES env variable");
		}

		obj = (JsonObject) ((JsonArray) dbEntry.getValue()).get(0);

		obj = (JsonObject) obj.get(SystemConstants.CREDENTIALS);



		final String username = obj.get(SystemConstants.USER_NAME).getAsString();
		logger.debug("USER_NAME: " + username);
		
		final String password = obj.get(SystemConstants.PASSWORD).getAsString();
		logger.debug("PASSWORD: " + password);
		
		final String jdbcUrl = obj.get(SystemConstants.JDBC_URL).getAsString();
		logger.debug("JDBC_URL: " + jdbcUrl);
		
		final int port = Integer.parseInt(obj.get(SystemConstants.PORT).getAsString());
		logger.debug("PORT: " + port);
		
		
		this.mysqlDataSource.setURL(jdbcUrl);
		this.mysqlDataSource.setUser(username);
		this.mysqlDataSource.setPassword(password);
		this.mysqlDataSource.setPortNumber(port);
		this.mysqlDataSource.setAllowMultiQueries(true);
		this.mysqlDataSource.setAutoClosePStmtStreams(true);
		this.mysqlDataSource.setAutoReconnect(true);

		

	}

}
