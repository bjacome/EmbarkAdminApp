package com.rbc.b2e.embark.admin.exception;

import com.rbc.b2e.embark.admin.util.SystemMessageHandler;


public class GenericEmbarkAdminException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -57723572393534537L;
	
		final private String status;
		final private int code;
		final private String message; 
	
	protected GenericEmbarkAdminException(String status, int code) {
		this.status = status;
		this.code = code;
		this.message = SystemMessageHandler.getMessage(Integer.toString(this.code));
	}
	
	protected GenericEmbarkAdminException(String status, int code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the code
	 */
	public int hashCode() {
		return code;
	}
	
	

}
