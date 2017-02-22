package com.rbc.b2e.embark.admin.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "_id", "_rev", "name", "title", "statuses", "checklist", "cohorts" })
public class Role extends Checklist {
	private Set<String> theStatuses;
	private List<Cohort> theCohorts;

	public Set<String> getStatuses() {
		return theStatuses;
	}

	public void setStatuses(Set<String> aStatuses) {
		theStatuses = aStatuses;
	}

	public List<Cohort> getCohorts() {
		return theCohorts;
	}

	public void setCohorts(List<Cohort> aCohorts) {
		theCohorts = aCohorts;
	}

	@Override
	public LabelType getType() {
		return LabelType.ROLE;
	}
}
