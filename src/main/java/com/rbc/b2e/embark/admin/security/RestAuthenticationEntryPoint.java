package com.rbc.b2e.embark.admin.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.util.SystemConstants;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static final Logger logger = LoggerFactory
			.getLogger(RestAuthenticationEntryPoint.class);

	final private ObjectMapper mapper = new ObjectMapper();

	@Override
	public void commence(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		logger.debug("RestAuthenticationEntryPoint  - commence");
		try {
			final Response responseObj = new Response();
			responseObj.setCode(HttpServletResponse.SC_UNAUTHORIZED);
			responseObj.setStatus(Response.Status.STATUS_FAIL);
			responseObj.setMessage(authException.getMessage());
			final String jsonInString = mapper.writeValueAsString(responseObj);
			response.setHeader(SystemConstants.CONTENT_TYPE,
					SystemConstants.CONTENT_TYPE_APPLICATION_JSON);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().print(jsonInString);
			response.getWriter().flush();
			logger.error(authException.getMessage(), authException);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

}
