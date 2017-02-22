package com.rbc.b2e.embark.admin.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.util.SystemConstants;

public class RestAuthenticationFailureHandler extends
		SimpleUrlAuthenticationFailureHandler implements AccessDeniedHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(RestAuthenticationFailureHandler.class);

	final private ObjectMapper mapper = new ObjectMapper();

	public void onAuthenticationFailure(
			javax.servlet.http.HttpServletRequest request,
			javax.servlet.http.HttpServletResponse response,
			AuthenticationException exception) {
		logger.debug("RestAuthenticationFailureHandler - onAuthenticationFailure");
		this.buildErrorMessage(exception, response, HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Override
	public void handle(HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException,
			ServletException {
		logger.debug("RestAuthenticationFailureHandler - onAccessDeniedException");
		this.buildErrorMessage(accessDeniedException, response, HttpServletResponse.SC_FORBIDDEN);

	}
	
	private void buildErrorMessage(Exception exception,
			javax.servlet.http.HttpServletResponse response, int code) {
		try {
			final Response responseObj = new Response();
			responseObj.setCode(code);
			responseObj.setStatus(Response.Status.STATUS_FAIL);
			responseObj.setMessage(exception.getMessage());
			final String jsonInString = mapper.writeValueAsString(responseObj);
			response.setHeader(SystemConstants.CONTENT_TYPE,
					SystemConstants.CONTENT_TYPE_APPLICATION_JSON);
			response.setStatus(code);
			response.getWriter().print(jsonInString);
			response.getWriter().flush();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

}
