/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InactiveServiceFactoryImpl.java
 * Author:	Jay Sangoi
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.events.InactiveBean;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class InactiveServiceFactoryImpl implements InactiveServiceFactory{

	private static final String DEFAULT_SECURITY_IN_ACTIVE = "DefaultSecurityInActive";

	/**
	 * 
	 */
	private static final long serialVersionUID = 5125347972773829491L;
	
	@Autowired
	private Map<String, InactiveService> inactiveServices;
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.inactive.InactiveServiceFactory#getInactiveService(com.smartstreamrdu.service.events.InactiveBean)
	 */
	@Override
	public InactiveService getInactiveService(InactiveBean bean) {
		if(bean != null && bean.getDatasource() != null && bean.getDbContainer() != null && bean.getDbContainer().getLevel() != null){
			String key = new StringBuilder().append(bean.getDatasource()).append(bean.getDbContainer().getLevel().name()).append("Inactive").toString();
			return inactiveServices.get(key);
		}
		return null;
	}

	@Override
	public InactiveService getSecurityInactiveService(InactiveBean bean) {
		if (bean != null && bean.getDatasource() != null && bean.getDbContainer() != null
				&& bean.getDbContainer().getLevel() != null) {
			return inactiveServices.get(DEFAULT_SECURITY_IN_ACTIVE);
		}
		return null;
	}

}
