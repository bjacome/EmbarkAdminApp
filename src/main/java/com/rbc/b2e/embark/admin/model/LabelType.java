package com.rbc.b2e.embark.admin.model;

public enum LabelType {
	ROLE, COHORT, TASKLIST, TASK;

	/**
	 * LabelType id's are their ordinal value (the integer corresponding to
	 * their position in the enum).
	 *
	 * @return
	 */
	public int id() {
		return ordinal();
	}
}
