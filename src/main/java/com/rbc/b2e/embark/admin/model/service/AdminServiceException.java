package com.rbc.b2e.embark.admin.model.service;

public class AdminServiceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AdminServiceException() {
		super();
	}

	public AdminServiceException(String aMessage, Exception aCause) {
		super(aMessage, aCause);
	}

	public AdminServiceException(String aMessage) {
		super(aMessage);
	}

	public AdminServiceException(Exception aCause) {
		super(aCause.getMessage());
	}
}
