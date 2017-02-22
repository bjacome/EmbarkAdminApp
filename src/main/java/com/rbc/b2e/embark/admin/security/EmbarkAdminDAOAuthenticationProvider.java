package com.rbc.b2e.embark.admin.security;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rbc.b2e.embark.admin.model.AdminUser;
import com.rbc.b2e.embark.admin.model.service.AdminServiceProvider;
import com.rbc.b2e.embark.admin.util.SystemConstants;
import com.rbc.b2e.embark.admin.util.SystemMessageHandler;


public class EmbarkAdminDAOAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(EmbarkAdminDAOAuthenticationProvider.class);
	
	AdminServiceProvider adminServiceProvider;

	private int maxLoginIntent;
	
	private PasswordEncoder passwordEncoder;
	
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		logger.debug("EmbarkAdminDAOAuthenticationProvider - authenticate");
		String username = authentication.getName();
		logger.debug(username);
		
		AdminUser adminUser = adminServiceProvider.getService().retrieveAdminUser(username);
		
		if (adminUser == null) {
			throw new BadCredentialsException(SystemMessageHandler.getMessage(SystemMessageHandler.INVALID_CREDENTIAL));
		}
		
		String password = (String) authentication.getCredentials();
		logger.debug(password);
		int loginIntent = adminUser.getFailedLoginAttempts();
		
		if (loginIntent >= this.maxLoginIntent ) {
			logger.error("EmbarkAdminDAOAuthenticationProvider: ", "ACCOUNT_LOCKED");
			throw new LockedException(SystemMessageHandler.getMessage(SystemMessageHandler.USER_LOCKED));
		}
		
		String encodedPassword = adminUser.getPassword();
		String tempPassword = adminUser.getTemporaryPassword();
		
		boolean matchPassword = encodedPassword != null && passwordEncoder.matches(password, encodedPassword);
		boolean matchTempPassword = tempPassword != null && passwordEncoder.matches(password, tempPassword);

		if (!matchPassword && !matchTempPassword) {
			logger.error("EmbarkAdminDAOAuthenticationProvider: ", "INVALID_CREDENTIALS");
			//adminUser.setFailedLoginAttempts(++loginIntent);
			//adminServiceProvider.getService().saveAdminUser(adminUser);
			throw new BadCredentialsException(SystemMessageHandler.getMessage(SystemMessageHandler.INVALID_CREDENTIAL));
		} else {
			if (loginIntent != 0 || tempPassword != null) {
				adminUser.setFailedLoginAttempts(0);
				adminUser.setTemporaryPassword(null);
				if (matchTempPassword) {
					adminUser.setPassword(tempPassword);
					adminUser.setPasswordValid(false);
				}
				adminServiceProvider.getService().saveAdminUser(adminUser);
			}
		}
		
		List<SimpleGrantedAuthority> grantedAuthority = new ArrayList<SimpleGrantedAuthority>();
		if (adminUser.isSuperUser() ) {
			grantedAuthority.add(new SimpleGrantedAuthority(SystemConstants.ROLE_SUPERUSER));
		} else {
			grantedAuthority.add(new SimpleGrantedAuthority(SystemConstants.ROLE_USER));
		}
		return new UsernamePasswordAuthenticationToken(adminUser, password, grantedAuthority);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

	/**
	 * @return the adminServiceProvider
	 */
	public AdminServiceProvider getAdminServiceProvider() {
		return adminServiceProvider;
	}

	/**
	 * @param adminServiceProvider the adminServiceProvider to set
	 */
	public void setAdminServiceProvider(AdminServiceProvider adminServiceProvider) {
		this.adminServiceProvider = adminServiceProvider;
	}

	/**
	 * @return the maxLoginIntent
	 */
	public int getMaxLoginIntent() {
		return maxLoginIntent;
	}

	/**
	 * @param maxLoginIntent the maxLoginIntent to set
	 */
	public void setMaxLoginIntent(int maxLoginIntent) {
		this.maxLoginIntent = maxLoginIntent;
	}

	/**
	 * @return the passwordEncoder
	 */
	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	/**
	 * @param passwordEncoder the passwordEncoder to set
	 */
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}



}
