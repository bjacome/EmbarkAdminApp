package com.rbc.b2e.embark.admin.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "_id", "key", "label", "taskList" })
public class TaskGroup extends Labeled {
	private List<Task> theTasks = Collections.emptyList();

	@JsonProperty("taskList")
	public List<Task> getTasks() {
		return theTasks;
	}

	public void setTasks(List<Task> aTasks) {
		theTasks = aTasks;
	}

	@Override
	public LabelType getType() {
		return LabelType.TASKLIST;
	}
}
