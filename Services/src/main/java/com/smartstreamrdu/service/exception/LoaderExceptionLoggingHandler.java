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

public interface LoaderExceptionLoggingHandler extends Serializable{
	
	void logException(Exception exception, String originClassName, Serializable dataObject);

}
