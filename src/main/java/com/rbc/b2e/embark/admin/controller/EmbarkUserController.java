package com.rbc.b2e.embark.admin.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;




public interface EmbarkUserController {

	static final String EMBARK_USER_REST_URI = "/rest/embark/";
	
	static final String GET_USERS_REST_URI = "users";
	
	static final String GET_USER_REST_URI = "user";
	
	static final String SAVE_USER_REST_URI = "save";
	
	static final String DELETE_USER_REST_URI = "delete";
	
	static final String IMPORT_USER_MULTIPART_URI = "import";

	static final String EXPORT_USER_REST_URI = "export";
	
	Response getUsers(Request<Map<?,?>> request, HttpServletRequest servletRequest) throws Exception;
	
	Response getUser(Request<Map<?,?>> request, HttpServletRequest servletRequest) throws Exception;
	
	Response updateUser(Request<Map<?,?>> request, HttpServletRequest servletRequest) throws Exception;
	
	Response removeUser(Request<Map<?,?>> request, HttpServletRequest servletRequest) throws Exception;

	Response importUsers(long environmentId, long roleId, long cohortId, MultipartFile file,
			HttpServletRequest servletRequest) throws Exception;
	
	void exportUsers(Request<Map<?,?>> request, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws Exception;

}
