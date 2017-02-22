package com.rbc.b2e.embark.admin.connection;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rbc.b2e.embark.admin.model.Environment;
import com.rbc.b2e.embark.admin.model.service.AdminService;
import com.rbc.b2e.embark.admin.model.service.AdminServiceProvider;

public class CloudantClientMgrProvider extends ConcurrentHashMap<Long, CloudantClientMgr>{

	private static final Logger logger = LoggerFactory.getLogger(CloudantClientMgrProvider.class);

	/**
	 * Default serialization 
	 */
	private static final long serialVersionUID = 1174793508101178269L;

	private final String databaseName;
	private final AdminService service;

	public CloudantClientMgrProvider(AdminServiceProvider adminServiceProvider, String databaseName) {
		super();
		this.databaseName = databaseName;
		this.service = adminServiceProvider.getService();
	}

	public CloudantClientMgr put(long key,String url, String user, String password) {
		logger.debug  ("Adding Client Manager");
		CloudantClientMgr ccm = CloudantClientMgr.getCloudantClientMgr(url, user, password, databaseName);
		return super.put(key, ccm);
	}

	@Override
	public CloudantClientMgr remove(Object key) {
		CloudantClientMgr ccm = super.remove(key);
		if (ccm != null) {
			ccm.shutdown();
		}
		return ccm;
	}

	public void init() {
		List<Environment> envList = service.retrieveEnvironments();
		for (Environment env : envList) {
			logger.debug("Init "+env.getName());
			try {
				this.put(env.getId(), env.getUrl(),  env.getUserName(), env.getPassword());
			} catch (Exception e) {
				logger.error("Failed to init "+env.getName());
			}
		}
	}

}
