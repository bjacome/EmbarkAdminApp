package com.rbc.b2e.embark.admin.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rbc.b2e.embark.admin.controller.CohortController;
import com.rbc.b2e.embark.admin.model.Cohort;
import com.rbc.b2e.embark.admin.model.service.AdminService;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.rest.Response.Status;

@Controller
@RequestMapping("/rest/admin/cohort/")
public class CohortControllerImpl extends AbstractController implements CohortController {

	private static final Logger logger = LoggerFactory.getLogger(CohortControllerImpl.class);

	@Override
	@RequestMapping(value = "save", method = { RequestMethod.OPTIONS, RequestMethod.POST })
	public @ResponseBody Response save(@RequestBody Request<Cohort> aRequest, HttpServletRequest servletRequest) {
		logger.debug("CohortController: save");
		Cohort cohort = aRequest.getData();

		Response response = new Response();
		if (cohort == null) {
			response.setStatus(Status.STATUS_ERROR);
			response.setMessage("No request data");
		} else {
			AdminService s = getAdminService();
			s.saveCohort(cohort);
			response.setData(cohort);
			response.setStatus(Status.STATUS_SUCCESS);
			return response;
		}
		return response;
	}

	@Override
	@RequestMapping(value = "delete", method = { RequestMethod.OPTIONS, RequestMethod.POST })
	public @ResponseBody Response delete(@RequestBody Request<Cohort> aRequest, HttpServletRequest servletRequest) {
		logger.debug("CohortController: delete");
		Cohort cohort = aRequest.getData();
		boolean existed = getAdminService().deleteCohort(cohort);
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

}
