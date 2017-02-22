package com.rbc.b2e.embark.admin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class AdminUserRole {
	private String theRole;
	private String theTitle;

	public String getRole() {
		return theRole;
	}

	public void setRole(String aRole) {
		theRole = aRole;
	}

	@JsonInclude(Include.NON_EMPTY)
	public String getTitle() {
		return theTitle;
	}

	public void setTitle(String aTitle) {
		theTitle = aTitle;
	}

}
