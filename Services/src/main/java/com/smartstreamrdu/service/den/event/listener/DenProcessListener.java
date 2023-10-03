/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DenProcessListener.java
 * Author:	Padgaonkar S
 * Date:	30-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.event.listener;

import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.service.event.process.listener.ProcessListener;

/**
 * Generic interface for den event listeners
 */
public interface DenProcessListener  extends ProcessListener{

	/**
	 * @param eventMessage
	 */
	public void onDenEventReceived(EventMessage eventMessage);
}
