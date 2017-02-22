
package com.rbc.b2e.embark.admin.controller.errorhandler;

import java.io.FileNotFoundException;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonParseException;
import com.rbc.b2e.embark.admin.exception.GenericEmbarkAdminException;
import com.rbc.b2e.embark.admin.model.service.AdminServiceError;
import com.rbc.b2e.embark.admin.model.service.AdminServiceException;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.util.SystemMessageHandler;
import com.sendgrid.SendGridException;

@ControllerAdvice
public class ControllerErrorHandler {

	private static final Logger logger = LoggerFactory.getLogger(ControllerErrorHandler.class);

	@ExceptionHandler(AccountExpiredException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public @ResponseBody Response handleAccountExpiredException(AccountExpiredException ex) {
		logger.error("LockedException: ", ex);
		return this.handleException(ex, HttpStatus.UNAUTHORIZED.value());
	}

	@ExceptionHandler(AdminServiceError.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody Response handleAdminServiceError(AdminServiceError ex) {
		logger.error("ServiceException: ", ex);
		Response response = new Response();
		response.setStatus(Response.Status.STATUS_FAIL);
		int code = ex.getCode();
		response.setMessage(SystemMessageHandler.getMessage(Integer.toString(code)));
		response.setCode(code);
		return response;
	}

	@ExceptionHandler(AdminServiceException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody Response handleAdminServiceException(AdminServiceException ex) {
		logger.error("ServiceException: ", ex);
		Response response = new Response();
		response.setStatus(Response.Status.STATUS_ERROR);
		response.setMessage(ex.getMessage());
		response.setCode(500);
		return response;
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public @ResponseBody Response handleAccountExpiredException(AccessDeniedException ex) {
		logger.error("LockedException: ", ex);
		return this.handleException(ex, HttpStatus.UNAUTHORIZED.value());
	}

	@ExceptionHandler(BadCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public @ResponseBody Response handleBadCredentialsException(BadCredentialsException ex) {
		logger.error("LockedException: ", ex);
		return this.handleException(ex, HttpStatus.UNAUTHORIZED.value());
	}

	@ExceptionHandler(LockedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public @ResponseBody Response handleLockedException(LockedException ex) {
		logger.error("LockedException: ", ex);
		return this.handleException(ex, HttpStatus.UNAUTHORIZED.value());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody Response handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		logger.error("HttpMessageNotReadableException: ", ex);
		return this.handleException(ex, HttpStatus.BAD_REQUEST.value());
	}

	@ExceptionHandler(FileNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public @ResponseBody Response handleFileNotFoundException(FileNotFoundException ex) {
		logger.error("FileNotFoundException: ", ex);
		return this.handleException(ex, HttpStatus.NOT_FOUND.value());
	}

	@ExceptionHandler(ServletException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public @ResponseBody Response handleServletException(ServletException ex) {
		logger.error("ServletException: ", ex);
		return this.handleException(ex, HttpStatus.NOT_FOUND.value());
	}

	@ExceptionHandler(JsonParseException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody Response handleJsonParseException(JsonParseException ex) {
		logger.error("JsonParseException: ", ex);
		return this.handleException(ex, HttpStatus.BAD_REQUEST.value());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	public @ResponseBody Response handleHttpRequestMethodNotSupportedException(
			HttpRequestMethodNotSupportedException ex) {
		logger.error("JsonParseException: ", ex);
		return this.handleException(ex, HttpStatus.METHOD_NOT_ALLOWED.value());
	}

	@ExceptionHandler(GenericEmbarkAdminException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public @ResponseBody Response handleGenericOnboardingException(GenericEmbarkAdminException ex) {
		logger.warn("GenericOnboardingException: ", ex);
		Response response = new Response();
		response.setStatus(ex.getStatus());
		response.setMessage(ex.getMessage());
		response.setCode(ex.hashCode());
		return response;
	}

	@ExceptionHandler(SendGridException.class)
	public @ResponseBody Response handleSendGridException(SendGridException ex) {
		logger.error("SendGridException: ", ex);
		return this.handleException(ex, 0);
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody Response handleSendGridException(Exception ex) {
		logger.error("Other Exception: ", ex);
		return this.handleException(ex, 0);
	}

	public Response handleException(Exception ex, int code) {
		Response response = new Response();
		response.setStatus(Response.Status.STATUS_ERROR);
		response.setMessage(SystemMessageHandler.getMessage(Integer.toString(code)));
		response.setCode(code);
		return response;
	}

}
