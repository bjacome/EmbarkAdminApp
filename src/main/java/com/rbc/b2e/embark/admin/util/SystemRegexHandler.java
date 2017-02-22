package com.rbc.b2e.embark.admin.util;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class SystemRegexHandler {
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("regex");
	
	public static final Pattern PASSWORD_PATTERN = Pattern.compile(BUNDLE.getString("password"));
}
