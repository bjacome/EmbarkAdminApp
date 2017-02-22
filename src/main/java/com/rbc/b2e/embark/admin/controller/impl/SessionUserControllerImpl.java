package com.rbc.b2e.embark.admin.controller.impl;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rbc.b2e.embark.admin.controller.SessionUserController;
import com.rbc.b2e.embark.admin.exception.OperationErrorException;
import com.rbc.b2e.embark.admin.exception.OperationFailException;
import com.rbc.b2e.embark.admin.model.AdminUser;
import com.rbc.b2e.embark.admin.model.Environment;
import com.rbc.b2e.embark.admin.model.Role;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.util.SystemConstants;
import com.rbc.b2e.embark.admin.util.SystemMessageHandler;
import com.rbc.b2e.embark.admin.util.SystemRegexHandler;


@Controller
@RequestMapping(SessionUserController.SESSION_USER_REST_URI)
public class SessionUserControllerImpl extends AbstractController implements
SessionUserController {

	private static final Logger logger = LoggerFactory.getLogger(SessionUserControllerImpl.class);

	@Override
	@RequestMapping(value = SessionUserController.UPDATE_PASSWORD_REST_URI, method = {RequestMethod.OPTIONS, RequestMethod.POST})
	public @ResponseBody Response setPassword(@RequestBody Request<Map<?,?>> aRequest, HttpServletRequest aServletRequest) throws Exception{

		PasswordEncoder passwordEncoder = (PasswordEncoder)getContext().getBean("passwordEncoder");
		try {
			Map<?,?> data = aRequest.getData();
			logger.debug(data.toString());
			if ( data != null) {
				String newPassword = data.get(SystemConstants.NEW_PASSWORD).toString();
				logger.debug(newPassword);
				String encodedNewPassowrd = passwordEncoder.encode(newPassword);
				String oldPassword = data.get(SystemConstants.OLD_PASSWORD).toString();
				logger.debug(oldPassword);
				AdminUser adminUser = (AdminUser)aServletRequest.getSession().getAttribute(SystemConstants.LOGGED_USER);
				logger.debug("adminUser" + (adminUser != null));
				if (!newPassword.equalsIgnoreCase(oldPassword) &&
						!encodedNewPassowrd.equalsIgnoreCase(adminUser.getPassword()) &&	
						SystemRegexHandler.PASSWORD_PATTERN.matcher(newPassword).matches()) {
					adminUser = getAdminService().retrieveAdminUser(adminUser.getEmail());
					adminUser.setPassword(encodedNewPassowrd);
					adminUser.setTemporaryPassword(null);
					adminUser.setFailedLoginAttempts(0);
					adminUser.setPasswordValid(true);
					getAdminService().updatePassword(adminUser);
				} else {
					logger.debug("UpdatePasswordBusiness - " + SystemMessageHandler.USER_NOT_FOUND  );
					throw new OperationFailException(SystemMessageHandler.INVALID_CREDENTIAL);
				}
			}
		} catch (NullPointerException e) {
			throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
		}
		return new Response();
	}

	@Override
	@RequestMapping(value = SessionUserController.ENVIRONMENTS_REST_URI, method = { RequestMethod.OPTIONS,
			RequestMethod.POST })
	public @ResponseBody Response getEnvironments(@RequestBody Request<Map<?,?>> aRequest, HttpServletRequest aServletRequest) {
		logger.debug("SessionUserController: getEnvironments");
		final List<Environment> list = getAdminService().retrieveEnvironmentsByAdminUser();
		final Response response = new Response();
		response.setData(list);
		return response;
	}

	@Override
	@RequestMapping(value = SessionUserController.ROLES_REST_URI, method = { RequestMethod.OPTIONS,
			RequestMethod.POST })
	public @ResponseBody Response getRoles(@RequestBody Request<Map<?, ?>> aRequest,
			HttpServletRequest aServletRequest) throws Exception {
		AdminUser adminUser = (AdminUser)aServletRequest.getSession().getAttribute(SystemConstants.LOGGED_USER);
		final List<Role> list = getAdminService().retrieveRolesByAdminUser(adminUser.getId(), adminUser.isSuperUser());
		final Response response = new Response();
		response.setData(list);
		return response;
	}



}
