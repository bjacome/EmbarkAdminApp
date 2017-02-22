package com.rbc.b2e.embark.admin.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rbc.b2e.embark.admin.util.SystemConstants;

public class JndiDatasourceProvider extends GenericDataSourceProvider implements DataSourceProvider {
	
	private static final Logger logger = LoggerFactory
			.getLogger(JndiDatasourceProvider.class);
	
	private final static String JDBC_STRING = "jdbc/";
	
	private DataSource dataSource = null;
	
	private String dbJNDIKey;
	
	public JndiDatasourceProvider(String serviceName, String vcapServices) {
			dbJNDIKey = this.getJndiName(serviceName, vcapServices);
	}

	public Connection getConnection() throws SQLException {	
		return dataSource.getConnection();
	}
	
	@Override
	public DataSource getDataSource()  throws Exception {
		Context context = new InitialContext();
		this.dataSource = (DataSource) context.lookup(dbJNDIKey);
		return this.dataSource;
	}
	
	private String getJndiName(String serviceName, String vcapServices){
		logger.debug("vcapServices is " + vcapServices);
		JsonObject obj = (JsonObject) new JsonParser().parse(vcapServices);
		Entry<String, JsonElement> dbEntry = null;
		Set<Entry<String, JsonElement>> entries = obj.entrySet();
		
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
		String databaseName = obj.get(SystemConstants.NAME).getAsString();
		return JDBC_STRING+databaseName;
	}

}
