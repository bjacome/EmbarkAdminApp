package com.rbc.b2e.embark.admin.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.rbc.b2e.embark.admin.exception.GenericEmbarkAdminException;
import com.rbc.b2e.embark.admin.exception.OperationErrorException;
import com.rbc.b2e.embark.admin.util.SystemMessageHandler;
import com.rbc.b2e.embark.admin.util.SystemRegexHandler;

public class EmbarkPasswordEncoder  {

	private PasswordEncoder thePasswordEncoder;
	
	/**
	 * It validates that the password is according with the RBC policy 
	 * @param aPassword
	 * @return an encoded password to be stored 
	 * @throws GenericEmbarkAdminException when the password has an invalid format
	 */
	public String encodePassword(String aPassword) throws GenericEmbarkAdminException {
		if (this.validatePassword(aPassword)) {
			return thePasswordEncoder.encode(aPassword);
		} else {
			throw new OperationErrorException(SystemMessageHandler.INVALID_PASSWORD_FORMAT);
		}
	}


	private boolean validatePassword(String newPassword) {
		return SystemRegexHandler.PASSWORD_PATTERN.matcher(newPassword).matches();
	}
	

	/**
	 * @param passwordEncoder the passwordEncoder to set
	 */
	public void setPasswordEncoder(PasswordEncoder aPasswordEncoder) {
		this.thePasswordEncoder = aPasswordEncoder;
	}


}
