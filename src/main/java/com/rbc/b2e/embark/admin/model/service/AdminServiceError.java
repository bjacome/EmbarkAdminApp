package com.rbc.b2e.embark.admin.model.service;

/**
 * Exception for User correctable errors.
 *
 * @author jdh
 *
 */
public class AdminServiceError extends AdminServiceException {
	private static final long serialVersionUID = 1L;

	private int theCode;

	public AdminServiceError(int aCode, String aReason) {
		super(aReason);
		theCode = aCode;
	}

	public int getCode() {
		return theCode;
	}
}
