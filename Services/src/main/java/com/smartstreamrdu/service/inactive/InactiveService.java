/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InactiveService.java
 * Author:	Jay Sangoi
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import java.io.Serializable;

import com.smartstreamrdu.events.InactiveBean;
import com.smartstreamrdu.exception.UdmBusinessException;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author Jay Sangoi
 *
 */
public interface InactiveService extends Serializable {
	/**
	 * Inactive the document(INS or LE) if required
	 * @param inactive
	 * @throws UdmTechnicalException 
	 * @throws Exception
	 */
	void inactivateIfRequired(InactiveBean inactive) throws UdmBusinessException, UdmTechnicalException;
	
}
