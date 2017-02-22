package com.rbc.b2e.embark.admin.connection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mysql.jdbc.Statement;

public interface DataSourceProvider {
	
	void closeConnection(Connection conn);
	
	void closeResultSet(ResultSet rs);
	
	void closeStatement(Statement st);
	
	Connection getConnection() throws SQLException;
	
	DataSource getDataSource()  throws Exception;

}
