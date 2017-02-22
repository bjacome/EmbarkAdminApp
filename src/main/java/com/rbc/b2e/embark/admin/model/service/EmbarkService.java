package com.rbc.b2e.embark.admin.model.service;

import java.util.List;
import java.util.Map;

import com.google.gson.internal.LinkedTreeMap;
import com.rbc.b2e.embark.admin.exception.CloudantGenericException;
import com.rbc.b2e.embark.admin.exception.GenericEmbarkAdminException;
import com.rbc.b2e.embark.admin.model.Cohort;

public interface EmbarkService {

	@SuppressWarnings("rawtypes")
	List<Map> getPage(String roleId, String cohortId, int pageSize, int page) throws Exception;
	
	Map<?,?> getUser(String id) throws Exception;
	
	Map<?,?> saveUser(Map<?,?> user) throws Exception;
	
	LinkedTreeMap<String,Object> importUsers(String fileName, Cohort cohort, String password, String createdBy) throws Exception;
	
	String exportView(String designName, String viewName, Cohort cohort) throws Exception;
	
	boolean removeUser(String id, String rev) throws CloudantGenericException, GenericEmbarkAdminException;
	
}
