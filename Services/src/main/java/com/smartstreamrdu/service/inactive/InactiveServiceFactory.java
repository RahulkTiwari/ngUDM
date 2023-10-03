/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InactiveServiceFactory.java
 * Author:	Jay Sangoi
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import java.io.Serializable;

import com.smartstreamrdu.events.InactiveBean;

/**
 * @author Jay Sangoi
 *
 */
public interface InactiveServiceFactory extends Serializable{
	
	/**
	 * Get the inactive service bean defined
	 * @param bean - 
	 * @return InactiveService if any inactive service is defined else null
	 */
	InactiveService getInactiveService(InactiveBean bean);
    
	/**
	 * Get SecurityInActivation Service as per bean defined
	 */
	InactiveService getSecurityInactiveService(InactiveBean inactive);
	
}
