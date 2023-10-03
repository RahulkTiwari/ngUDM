
/**
 * *****************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionMessageHandler.java
 * Author:	Padgaonkar
 * Date:	28-Feb-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.exceptions;

/**
 * This method is used to send UdmExceptionMessage to  UdmExceptionMessageQueue.
 * @param data
 */
public interface UdmExceptionMessageHandler {
	
	public void sendMessage(UdmExceptionDataHolder data);

}
