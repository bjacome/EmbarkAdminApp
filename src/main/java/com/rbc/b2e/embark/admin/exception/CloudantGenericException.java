package com.rbc.b2e.embark.admin.exception;

import com.rbc.b2e.embark.admin.util.SystemMessageHandler;

public class CloudantGenericException extends Exception {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 3986705784525161813L;

	public CloudantGenericException() {
		super(SystemMessageHandler.getMessage(SystemMessageHandler.EMBARK_USER_NOSQL_ERROR));
	}

	public CloudantGenericException(String message) {
		super(message);
	}

	public CloudantGenericException(Throwable cause) {
		super(cause);
	}

	public CloudantGenericException(String message, Throwable cause) {
		super(message, cause);
	}

	public CloudantGenericException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
