/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	VfsMessageSender.java
 * Author:	Shruti Arora
 * Date:	05-Nov-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.openfigi;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;


/**
 * @author SArora
 * 
 * This service is responsible for sending the message to specified process and partition using kakfka
 *
 */
public interface VfsMessageSender  {
	void sendMessage(VfsFigiRequestMessage identifiers, com.smartstreamrdu.util.Constant.Process process,
			Integer partition);

}
