package com.rbc.b2e.embark.admin.connection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.Statement;

public abstract class GenericDataSourceProvider implements DataSourceProvider {

	private static final Logger logger = LoggerFactory
			.getLogger(GenericDataSourceProvider.class); 

	@Override
	public void closeConnection(Connection conn) {
		if (conn!=null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				logger.error(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public void closeStatement(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				logger.error(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public abstract Connection getConnection() throws SQLException;

	@Override
	public abstract DataSource getDataSource() throws Exception;

}
