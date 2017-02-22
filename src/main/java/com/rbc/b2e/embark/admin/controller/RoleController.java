package com.rbc.b2e.embark.admin.controller;

import javax.servlet.http.HttpServletRequest;

import com.rbc.b2e.embark.admin.model.Role;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;

public interface RoleController {

	Response retrieveAll(Request<?> aRequest, HttpServletRequest aHttpRequest);

	Response save(Request<Role> aRequest, HttpServletRequest aHttpRequest);

	Response delete(Request<Role> aRequest, HttpServletRequest aHttpRequest);
}
