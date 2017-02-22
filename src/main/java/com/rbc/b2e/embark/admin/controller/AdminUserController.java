package com.rbc.b2e.embark.admin.controller;

import javax.servlet.http.HttpServletRequest;

import com.rbc.b2e.embark.admin.model.AdminUser;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;

public interface AdminUserController {

	static final String ADMIN_USER_REST_URI = "/rest/admin/adminUser/";
	
	static final String SAVE_ADMIN_USER_REST_URI = "save";
	
	static final String DELETE_ADMIN_USER_REST_URI = "delete";
	
	static final String RETRIEVE_ADMIN_USER_REST_URI = "retrieve-all";
	

	Response save(Request<AdminUser> aRequest, HttpServletRequest aHttpRequest);

	Response delete(Request<AdminUser> aRequest, HttpServletRequest aHttpRequest);

	Response retrieve(Request<?> aRequest, HttpServletRequest aServletRequest);
}
