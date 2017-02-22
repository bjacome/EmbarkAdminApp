package com.rbc.b2e.embark.admin.exception;

import com.rbc.b2e.embark.admin.rest.Response;


public class OperationFailException extends GenericEmbarkAdminException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8943334445026318269L;

	public OperationFailException(int code) {
		super(Response.Status.STATUS_FAIL, code);
	}

}
