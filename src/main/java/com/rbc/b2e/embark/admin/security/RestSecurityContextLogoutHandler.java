package com.rbc.b2e.embark.admin.security;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.util.SystemConstants;

public class RestSecurityContextLogoutHandler implements LogoutSuccessHandler {

	private static final Logger logger = LoggerFactory.getLogger(RestSecurityContextLogoutHandler.class);
	
	final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void onLogoutSuccess(javax.servlet.http.HttpServletRequest request,
			javax.servlet.http.HttpServletResponse response,
			Authentication authentication) {
		this.buildResponse(response);
	}

	private void buildResponse(javax.servlet.http.HttpServletResponse response) {
		try {
			final Response responseObj = new Response();
			responseObj.setCode(HttpServletResponse.SC_OK);
			responseObj.setStatus(Response.Status.STATUS_SUCCESS);
			final String jsonInString = mapper.writeValueAsString(responseObj);
			response.setHeader(SystemConstants.CONTENT_TYPE,SystemConstants.CONTENT_TYPE_APPLICATION_JSON);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().print(jsonInString);
			response.getWriter().flush();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(),e);
		}
	}

}
