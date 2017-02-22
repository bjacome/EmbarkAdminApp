package com.rbc.b2e.embark.admin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "_id", "_rev", "name", "location", "url", "host", "port", "userName", "password" })
public class Environment {
	private long theId;
	private long theRev;
	private String theName;
	private String theLocation;
	private String theUrl;
	private String theHost;
	private int thePort;
	private String theUserName;
	private String thePassword;

	public String getLocation() {
		return theLocation;
	}

	public void setLocation(String aLocation) {
		theLocation = aLocation;
	}

	public String getUrl() {
		return theUrl;
	}

	public void setUrl(String aUrl) {
		theUrl = aUrl;
	}

	public String getHost() {
		return theHost;
	}

	public void setHost(String aHost) {
		theHost = aHost;
	}

	public int getPort() {
		return thePort;
	}

	public void setPort(int aPort) {
		thePort = aPort;
	}

	public String getUserName() {
		return theUserName;
	}

	public void setUserName(String aUserName) {
		theUserName = aUserName;
	}

	public String getPassword() {
		return thePassword;
	}

	public void setPassword(String aPassword) {
		thePassword = aPassword;
	}

	@JsonProperty("_id")
	public long getId() {
		return theId;
	}

	@JsonProperty("_rev")
	public long getRevision() {
		return theRev;
	}

	public void setId(long aId) {
		theId = aId;
	}

	public void setRevision(long aRev) {
		theRev = aRev;
	}

	public String getName() {
		return theName;
	}

	public void setName(String aLabel) {
		theName = aLabel;
	}
}
