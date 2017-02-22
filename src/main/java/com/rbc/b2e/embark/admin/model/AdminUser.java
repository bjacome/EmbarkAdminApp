package com.rbc.b2e.embark.admin.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "_id", "_rev", "email", "fullName", "superUser", "roles" })
public class AdminUser {
	private long theId;
	private long theRev;
	private String theEmail;
	private String theFullName;
	private boolean theSuperUserIndicator;
	private List<AdminUserRole> theRoles = Collections.emptyList();
	private String theTemporaryPassword;
	private String thePassword;
	private int theFailedLoginAttempts;
	private boolean thePasswordValid;

	@JsonProperty("_id")
	public long getId() {
		return theId;
	}

	public String getEmail() {
		return theEmail;
	}

	public String getFullName() {
		return theFullName;
	}

	public boolean isSuperUser() {
		return theSuperUserIndicator;
	}

	public String getTemporaryPassword() {
		return theTemporaryPassword;
	}

	@JsonIgnore
	public int getFailedLoginAttempts() {
		return theFailedLoginAttempts;
	}

	public void setSuperUser(boolean aSuperUserIndicator) {
		theSuperUserIndicator = aSuperUserIndicator;
	}

	public void setId(long aId) {
		theId = aId;
	}

	public void setEmail(String aEmail) {
		theEmail = aEmail;
	}

	public void setFullName(String aFullName) {
		theFullName = aFullName;
	}

	public void setTemporaryPassword(String aTemporaryPassword) {
		theTemporaryPassword = aTemporaryPassword;
	}

	public void setFailedLoginAttempts(int aFailedLoginAttempts) {
		theFailedLoginAttempts = aFailedLoginAttempts;
	}

	@JsonIgnore
	public String getPassword() {
		return thePassword;
	}

	public void setPassword(String aPassword) {
		thePassword = aPassword;
	}

	@JsonProperty("_rev")
	public long getRevision() {
		return theRev;
	}

	public void setRevision(long aRev) {
		theRev = aRev;
	}

	@JsonProperty("roles")
	public List<AdminUserRole> getRoles() {
		return theRoles;
	}

	public void setRoles(List<AdminUserRole> aRoles) {
		theRoles = aRoles;
	}

	@JsonIgnore
	public boolean isPasswordValid() {
		return this.thePasswordValid;
	}

	public void setPasswordValid(boolean isValid) {
		this.thePasswordValid = isValid;
	}
}
