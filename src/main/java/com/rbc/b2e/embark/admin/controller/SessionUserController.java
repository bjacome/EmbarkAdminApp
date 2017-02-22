package com.rbc.b2e.embark.admin.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;

public interface SessionUserController {
	//Session
	static final String SESSION_USER_REST_URI = "/rest/session/";
	
	static final String UPDATE_PASSWORD_REST_URI = "updatePassword";
	
	static final String ROLES_REST_URI = "roles";
	
	static final String ENVIRONMENTS_REST_URI = "environments";

	Response setPassword(Request<Map<?,?>> aRequest, HttpServletRequest aServletRequest) throws Exception;

	Response getRoles(Request<Map<?,?>> aRequest, HttpServletRequest aServletRequest) throws Exception;

	Response getEnvironments(Request<Map<?,?>> aRequest, HttpServletRequest aServletRequest) throws Exception;

	
}
