/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LoaderExceptionLoggingHandler.java
 * Author:	Rushikesh Dedhia
 * Date:	18-Dec-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exception;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.Record;

@Component
public class LoaderExceptionLoggingHandlerImpl implements LoaderExceptionLoggingHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6284265943407838285L;
	private static final Logger logger = LoggerFactory.getLogger(LoaderExceptionLoggingHandlerImpl.class);

	@Override
	public void logException(Exception exception, String originClassName, Serializable dataObject) {
		logger.error("Following error occured while processing {} : {} in the class {}. This object will not be processed further.", 
				dataObject.getClass().getSimpleName(), dataObject, originClassName, exception);

		// Add logic to send messages to a dead letter queue.
		
		// If the data object is of type Record, then we mark the same as not to be processed.
		if (dataObject instanceof Record) {
			((Record) dataObject).setToBeProcessed(false);
		}
	}

}
