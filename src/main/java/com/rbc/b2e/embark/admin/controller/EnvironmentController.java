package com.rbc.b2e.embark.admin.controller;

import javax.servlet.http.HttpServletRequest;

import com.rbc.b2e.embark.admin.model.Environment;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;

public interface EnvironmentController {
	
	static final String SESSION_ENVIRONMENT_REST_URI = "/rest/admin/environment/";
	
	static final String SAVE_ENVIRONMENT_REST_URI = "save";
	
	static final String DELETE_ENVIRONMENT_REST_URI = "delete";
	
	static final String RETRIEVE_ENVIRONMENTS_REST_URI = "retrieve-all";

	Response retrieveAll(Request<?> aRequest, HttpServletRequest aHttpRequest);

	Response save(Request<Environment> aRequest, HttpServletRequest aHttpRequest) throws Exception;

	Response delete(Request<Environment> aRequest, HttpServletRequest aHttpRequest);
}
