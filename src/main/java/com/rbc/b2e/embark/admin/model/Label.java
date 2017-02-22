package com.rbc.b2e.embark.admin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "lang", "text" })
@JsonInclude(Include.NON_EMPTY)
public class Label {
	private String theLanguage;
	private String theText;

	@JsonProperty("lang")
	public String getLanguage() {
		return theLanguage;
	}

	public void setLanguage(String aLanguage) {
		theLanguage = aLanguage;
	}

	public String getText() {
		return theText;
	}

	public void setText(String aText) {
		theText = aText;
	}
}
