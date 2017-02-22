package com.rbc.b2e.embark.admin.model;

import java.io.Serializable;

public class UpdatePasswordRequest implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2408282843808641123L;
	
	
	private String newPassword;
	private String currentPassword;



	/**
	 * @return the newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * @param newPassword the newPassword to set
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	/**
	 * @return the currentPassword
	 */
	public String getCurrentPassword() {
		return currentPassword;
	}

	/**
	 * @param currentPassword the currentPassword to set
	 */
	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}
	
}
