package com.rbc.b2e.embark.admin.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Checklist is either a Role or a Cohort.
 *
 * @author jdh
 *
 */
public abstract class Checklist extends Labeled {

	private List<TaskGroup> theTaskGroups = Collections.emptyList();
	private long theRev;

	@JsonProperty("checklist")
	public List<TaskGroup> getTaskGroups() {
		return theTaskGroups;
	}

	public void setTaskGroups(List<TaskGroup> aTaskGroups) {
		theTaskGroups = aTaskGroups;
	}

	@JsonProperty("_rev")
	public long getRevision() {
		return theRev;
	}

	public void setRevision(long aRev) {
		theRev = aRev;
	}

	@Override
	@JsonProperty("name")
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}

	@Override
	@JsonProperty("title")
	public List<Label> getLabels() {
		return super.getLabels();
	}
}
