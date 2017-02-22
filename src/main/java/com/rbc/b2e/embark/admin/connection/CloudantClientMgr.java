package com.rbc.b2e.embark.admin.connection;

import org.lightcouch.CouchDbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;

public class CloudantClientMgr {

	private static final Logger logger = LoggerFactory.getLogger(CloudantClientMgr.class);

	private  CloudantClient cloudant = null;
	private  Database db = null;

	private final String url;
	private final String user;
	private final String password;
	private final String databaseName;

	public static CloudantClientMgr getCloudantClientMgr(String url, String user, String password, String databaseName) {
		logger.debug("getCloudantClientMgr to get a new CloudantClientMgr");
		CloudantClientMgr ccm = new CloudantClientMgr( url,  user,  password,  databaseName);
		ccm.initClient();
		return ccm;
	}
	
	private CloudantClientMgr(String url, String user, String password, String databaseName) {
		this.url = url;
		this.user = user;
		this.password =  password;
		this.databaseName = databaseName;
	}

	private  void initClient() {
		if (cloudant == null) {
			synchronized (CloudantClientMgr.class) {
				if (cloudant != null) {
					return;
				}
				cloudant = this.createClient();
				logger.info("CloudantClientMgr initiated - " + this.getDatabaseURL());
			}// end synchronized
		}
	}
	
	public void shutdown() {
		if (cloudant != null) {
			cloudant.shutdown();
		}
	}
	
	private CloudantClient createClient() {
		try {
			return new CloudantClient(user, user, password);
		} catch (CouchDbException e) {
			logger.error("CloudantClientMgr- CouchDbException: Unable to connect to repository", e);
			throw new RuntimeException("Unable to connect to repository", e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("CloudantClientMgr- Unable to connect to repository", e);
			throw new RuntimeException("Unable to connect to repository", e);
		}
	}

	public Database getDB() {
		if (cloudant == null) {
			initClient();
		}

		if (db == null) {
			try {
				db = cloudant.database(databaseName, true);
			} catch (Exception e) {
				throw new RuntimeException("DB Not found", e);
			}
		}
		return db;
	}

	public String getDatabaseURL() {
		return url + "/" + databaseName + "/";
	}

	public String getServerURL() {
		return db.getDBUri().toString();
	}
}
