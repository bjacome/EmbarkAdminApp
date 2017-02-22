package com.rbc.b2e.embark.admin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "_id", "_rev", "role","roleName", "name", "title", "startDate", "disableDate", "checklist" })
public class Cohort extends Checklist {

	private long theRoleId;
	private String theRoleName;
	private String theStartDate;
	private String theDisableDate;

	public String getStartDate() {
		return theStartDate;
	}

	public String getDisableDate() {
		return theDisableDate;
	}

	public void setStartDate(String aStartDate) {
		theStartDate = aStartDate;
	}

	public void setDisableDate(String aDisableDate) {
		theDisableDate = aDisableDate;
	}

	
	public String getRoleName() {
		return theRoleName;
	}

	public void setRoleName(String aRole) {
		theRoleName = aRole;
	}
	
	@JsonProperty("role")
	public long getRoleId() {
		return this.theRoleId;
	}
	
	public void setRoleId(long aRoleId) {
		this.theRoleId = aRoleId;
	}

	@Override
	public LabelType getType() {
		return LabelType.COHORT;
	}
}
