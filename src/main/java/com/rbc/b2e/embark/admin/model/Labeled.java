package com.rbc.b2e.embark.admin.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Labeled {

	private long theId;
	private String theName;
	private List<Label> theLabels = Collections.emptyList();

	@JsonProperty("_id")
	public long getId() {
		return theId;
	}

	@JsonProperty("key")
	public String getName() {
		return theName;
	}

	public void setId(long aId) {
		theId = aId;
	}

	public void setName(String aLabel) {
		theName = aLabel;
	}

	@JsonProperty("label")
	public List<Label> getLabels() {
		return theLabels;
	}

	public void setLabels(List<Label> aLabels) {
		theLabels = aLabels;
	}

	@JsonIgnore
	public abstract LabelType getType();

}
