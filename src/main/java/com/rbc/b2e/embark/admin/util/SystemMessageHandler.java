package com.rbc.b2e.embark.admin.util;

import java.util.ResourceBundle;

public class SystemMessageHandler {

	public static final int INVALID_PARAMETERS = 1000301;
	
	public static final int INVALID_REQUEST = 1000701;
	public static final int INVALID_CREDENTIAL = 1000702;
	public static final int USER_NOT_FOUND = 1000703;
	public static final int USER_LOCKED = 1000704;
	public static final int USER_DISABLED = 1000705;
	public static final int INVALID_PASSWORD_FORMAT = 1000706;
	
	
	public static final int EMBARK_USER_EXISTS = 1000802;
	public static final int EMBARK_USER_EMAIL_REQUIRED = 1000803;
	public static final int EMBARK_USER_NOSQL_ERROR = 1000899;
	
	
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages");
	
	public static String getMessage(String code) {
		return BUNDLE.getString(code);
	}
	
	public static String getMessage(int code) {
		return BUNDLE.getString(Integer.toString(code));
	}
	  
}
