package com.rbc.b2e.embark.admin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "_id", "key", "label", "dueDate" })
public class Task extends Labeled {
	private String theDueDate;

	public String getDueDate() {
		return theDueDate;
	}

	public void setDueDate(String aDueDate) {
		theDueDate = aDueDate;
	}

	@Override
	public LabelType getType() {
		return LabelType.TASK;
	}
}
