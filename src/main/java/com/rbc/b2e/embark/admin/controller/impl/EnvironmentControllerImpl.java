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

import com.rbc.b2e.embark.admin.connection.CloudantClientMgrProvider;
import com.rbc.b2e.embark.admin.controller.EnvironmentController;
import com.rbc.b2e.embark.admin.exception.OperationErrorException;
import com.rbc.b2e.embark.admin.model.Environment;
import com.rbc.b2e.embark.admin.model.service.AdminService;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.rest.Response.Status;
import com.rbc.b2e.embark.admin.util.SystemMessageHandler;

@Controller
@RequestMapping(EnvironmentController.SESSION_ENVIRONMENT_REST_URI)
public class EnvironmentControllerImpl extends AbstractController implements EnvironmentController {

	private static final Logger logger = LoggerFactory.getLogger(EnvironmentControllerImpl.class);

	@Override
	@RequestMapping(value = EnvironmentController.SAVE_ENVIRONMENT_REST_URI, method = { RequestMethod.OPTIONS,
			RequestMethod.POST })
	public @ResponseBody Response save(@RequestBody Request<Environment> aRequest, HttpServletRequest servletRequest)
			throws Exception {
		logger.debug("EnvironmentController: save");
		Environment env = aRequest.getData();

		Response response = new Response();

		try {
			AdminService s = getAdminService();
			env = s.saveEnvironment(env);
			CloudantClientMgrProvider cloudantClientMgrProvider = getContext().getBean(CloudantClientMgrProvider.class);
			if (cloudantClientMgrProvider.contains(env.getId())) {
				cloudantClientMgrProvider.remove(env.getId());
			}
			cloudantClientMgrProvider.put(env.getId(), env.getUrl(), env.getUserName(), env.getPassword());
			response.setData(env);
			return response;

		} catch (Exception e) {
			throw new OperationErrorException(SystemMessageHandler.INVALID_REQUEST);
		}
	}

	@Override
	@RequestMapping(value = EnvironmentController.DELETE_ENVIRONMENT_REST_URI, method = { RequestMethod.OPTIONS,
			RequestMethod.POST })
	public @ResponseBody Response delete(@RequestBody Request<Environment> aRequest,
			HttpServletRequest servletRequest) {
		logger.debug("EnvironmentController: delete");
		Environment env = aRequest.getData();
		getAdminService().deleteEnvironment(env.getId());
		CloudantClientMgrProvider cloudantClientMgrProvider = getContext().getBean(CloudantClientMgrProvider.class);
		cloudantClientMgrProvider.remove(env.getId());
		Response response = new Response();
		response.setStatus(Status.STATUS_SUCCESS);
		return response;
	}

	@Override
	@RequestMapping(value = EnvironmentController.RETRIEVE_ENVIRONMENTS_REST_URI, method = { RequestMethod.OPTIONS,
			RequestMethod.POST })
	public @ResponseBody Response retrieveAll(@RequestBody Request<?> aRequest, HttpServletRequest aServletRequest) {
		logger.debug("EnvironmentController: retrieve-all");
		// Object ignored = aRequest.getData();
		List<Environment> list = getAdminService().retrieveEnvironments();
		Response response = new Response();
		response.setStatus(Status.STATUS_SUCCESS);
		response.setData(list);
		return response;
	}
}
