package com.rbc.b2e.embark.admin.model.service.sql;

import java.sql.Connection;
import java.util.Map;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.objectof.sql.Sql;

import com.rbc.b2e.embark.admin.connection.DataSourceProvider;
import com.rbc.b2e.embark.admin.model.service.AdminService;
import com.rbc.b2e.embark.admin.model.service.AdminServiceException;
import com.rbc.b2e.embark.admin.model.service.AdminServiceProvider;

public class SqlAdminServer implements AdminServiceProvider {

	private static final Logger logger = LoggerFactory
			.getLogger(SqlAdminServer.class);
	
	private Map<String, String> theStatements;
	private DataSource theDataSource;
	private DataSourceProvider theProvider;

	public SqlAdminServer(DataSourceProvider aProvider) {
		try {
			theDataSource = aProvider.getDataSource();
			theStatements = Sql.map(ResourceBundle.getBundle(SqlAdminService.class.getSimpleName()));
			theProvider = aProvider;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	protected SqlAdminServer(DataSource aDataSource, Map<String, String> aStatements) {
		theDataSource = aDataSource;
		theStatements = aStatements;
	}

	public AdminService getService() {
		try {
			Connection conn = theDataSource.getConnection();
			if (conn.isValid(0)) {
				return new SqlAdminService(conn, theStatements);
			} else {
				theDataSource = theProvider.getDataSource();
				return getService();
			}
		} catch (Exception e) {
			throw new AdminServiceException(e);
		}
	}
}
