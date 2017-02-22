package com.rbc.b2e.embark.admin.controller.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rbc.b2e.embark.admin.controller.RoleController;
import com.rbc.b2e.embark.admin.model.Role;
import com.rbc.b2e.embark.admin.model.service.AdminService;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.rest.Response.Status;

@Controller
@RequestMapping("/rest/admin/role/")
public class RoleControllerImpl extends AbstractController implements RoleController {

	private static final Logger logger = LoggerFactory.getLogger(RoleControllerImpl.class);

	@Override
	@RequestMapping(value = "save", method = { RequestMethod.OPTIONS, RequestMethod.POST })
	public @ResponseBody Response save(@RequestBody Request<Role> aRequest, HttpServletRequest servletRequest) {
		logger.debug("RoleController: save");
		Role role = aRequest.getData();

		Response response = new Response();
		if (role == null) {
			response.setStatus(Status.STATUS_ERROR);
			response.setMessage("No request data");
		} else {
			AdminService s = getAdminService();
			s.saveRole(role);
			response.setData(role);
			response.setStatus(Status.STATUS_SUCCESS);
			return response;
		}
		return response;
	}

	@Override
	@RequestMapping(value = "delete", method = { RequestMethod.OPTIONS, RequestMethod.POST })
	public @ResponseBody Response delete(@RequestBody Request<Role> aRequest, HttpServletRequest servletRequest) {
		logger.debug("RoleController: delete");
		Role role = aRequest.getData();
		boolean existed = getAdminService().deleteRole(role);
		Response response = new Response();
		if (existed) {
			response.setStatus(Status.STATUS_SUCCESS);
		} else {
			response.setStatus(Status.STATUS_ERROR);
			response.setMessage(
					"Either the object does not exist or has been updated/deleted by another user or program.");
		}
		return response;
	}

	@Override
	@RequestMapping(value = "retrieve-all", method = { RequestMethod.OPTIONS, RequestMethod.POST })
	public @ResponseBody Response retrieveAll(@RequestBody Request<?> aRequest, HttpServletRequest aServletRequest) {
		logger.debug("RoleController: retrieve-all");
		List<Role> list = getAdminService().retrieveRoles();
		Response response = new Response();
		response.setStatus(Status.STATUS_SUCCESS);
		response.setData(list);
		return response;
	}
}
