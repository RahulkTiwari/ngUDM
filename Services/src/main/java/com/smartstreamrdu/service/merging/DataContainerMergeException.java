/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerMergeException.java
 * Author:	Jay Sangoi
 * Date:	24-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author Jay Sangoi
 *
 */
public class DataContainerMergeException extends UdmTechnicalException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5280748424557668202L;
	
	public DataContainerMergeException(String message) {
		super(message, null);
	}
	
	public DataContainerMergeException(String message, Exception cause) {
		super(message, cause);
	}

}
