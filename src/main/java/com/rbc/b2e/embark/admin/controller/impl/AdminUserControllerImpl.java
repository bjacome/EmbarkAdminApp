package com.rbc.b2e.embark.admin.controller.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rbc.b2e.embark.admin.controller.AdminUserController;
import com.rbc.b2e.embark.admin.model.AdminUser;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.rest.Response.Status;

@Controller
@RequestMapping(AdminUserController.ADMIN_USER_REST_URI)
public class AdminUserControllerImpl extends AbstractController implements AdminUserController {

	private static final Logger logger = LoggerFactory.getLogger(AdminUserControllerImpl.class);

	@Override
	@RequestMapping(value = AdminUserController.SAVE_ADMIN_USER_REST_URI, method = { RequestMethod.OPTIONS,
			RequestMethod.POST })
	public @ResponseBody Response save(@RequestBody Request<AdminUser> aRequest, HttpServletRequest servletRequest) {
		logger.debug("AdminUserController: create");
		AdminUser user = aRequest.getData();
		String tempPassword = user.getTemporaryPassword();
		user = getAdminService().saveAdminUser(user);
		if (tempPassword != null && tempPassword.length() > 0) {
			PasswordEncoder passwordEncoder = getContext().getBean(PasswordEncoder.class);
			tempPassword = passwordEncoder.encode(tempPassword);
			user.setTemporaryPassword(tempPassword);
			user.setPasswordValid(false);
			user = getAdminService().updateTemporaryPassword(user);
		}
		user.setTemporaryPassword(null);
		Response response = new Response();
		response.setStatus(Status.STATUS_SUCCESS);
		response.setData(user);
		return response;
	}

	@Override
	@RequestMapping(value = AdminUserController.DELETE_ADMIN_USER_REST_URI, method = { RequestMethod.OPTIONS,
			RequestMethod.POST })
	public @ResponseBody Response delete(@RequestBody Request<AdminUser> aRequest, HttpServletRequest servletRequest) {
		logger.debug("AdminUserController: delete");
		AdminUser user = aRequest.getData();
		getAdminService().deleteAdminUser(user.getId());
		Response response = new Response();
		response.setStatus(Status.STATUS_SUCCESS);
		response.setData(null);
		return response;
	}

	@Override
	@RequestMapping(value = AdminUserController.RETRIEVE_ADMIN_USER_REST_URI, method = { RequestMethod.OPTIONS,
			RequestMethod.POST })
	public @ResponseBody Response retrieve(@RequestBody Request<?> aRequest, HttpServletRequest aServletRequest) {
		logger.debug("AdminUserController: retrieve-all");
		List<AdminUser> list = getAdminService().retrieveAdminUsers();
		for (AdminUser user : list) {
			/*
			 * special case - we can't use JsonIgonore annotation because we
			 * need to deserialize it from client requests.
			 */
			user.setTemporaryPassword(null);
		}
		Response response = new Response();
		response.setStatus(Status.STATUS_SUCCESS);
		response.setData(list);
		return response;
	}
}
