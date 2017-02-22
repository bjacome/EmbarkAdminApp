package com.rbc.b2e.embark.admin.controller;

import javax.servlet.http.HttpServletRequest;

import com.rbc.b2e.embark.admin.model.Cohort;
import com.rbc.b2e.embark.admin.rest.Request;
import com.rbc.b2e.embark.admin.rest.Response;

public interface CohortController {

	Response save(Request<Cohort> aRequest, HttpServletRequest aHttpRequest);

	Response delete(Request<Cohort> aRequest, HttpServletRequest aHttpRequest);
}
