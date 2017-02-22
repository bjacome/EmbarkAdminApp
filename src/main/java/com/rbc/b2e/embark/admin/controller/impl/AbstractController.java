package com.rbc.b2e.embark.admin.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.rbc.b2e.embark.admin.model.service.AdminService;
import com.rbc.b2e.embark.admin.model.service.AdminServiceProvider;

public class AbstractController implements ApplicationContextAware {
	private ApplicationContext theContext;

	private static final Logger logger = LoggerFactory.getLogger(AbstractController.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		theContext = applicationContext;
	}

	protected ApplicationContext getContext() {
		return theContext;
	}

	protected AdminService getAdminService() {
		logger.debug(this.getClass().getCanonicalName()+" - getAdminService");
		return getContext().getBean(AdminServiceProvider.class).getService();
	}
}
