package com.rbc.b2e.embark.admin.exception;

import com.rbc.b2e.embark.admin.rest.Response;


public class OperationErrorException extends GenericEmbarkAdminException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1573501751626266441L;

	public OperationErrorException(int code) {
		super(Response.Status.STATUS_ERROR, code);
	}

}
