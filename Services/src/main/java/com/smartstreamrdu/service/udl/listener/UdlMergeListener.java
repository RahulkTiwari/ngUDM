/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdlMergeListener.java
 * Author:	Padgaonkar S
 * Date:	01-Nov-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.udl.listener;

import com.smartstreamrdu.service.event.process.listener.ProcessListener;

/**
 * Generic listeners interface getting called post dataContainerMerging
 * @author Padgaonkar
 *
 */
public interface UdlMergeListener extends ProcessListener {

	/**
	 * 
	 * @param input
	 */
	public void postMergingContainer(UdlProcessListenerInput input);

}
