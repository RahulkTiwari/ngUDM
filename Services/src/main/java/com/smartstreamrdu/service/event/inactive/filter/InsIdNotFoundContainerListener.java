/*******************************************************************
*
* Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	InsIdNotFoundContainerListener.java
* Author:Padgaonkar S
* Date:	05-Jan-2022
*
*******************************************************************
*/
package com.smartstreamrdu.service.event.inactive.filter;

import com.smartstreamrdu.service.event.process.listener.ProcessListener;

/**
 * Udl event related to filtering inactive Container
 * @author Padgaonkar
 * 
 */
public interface InsIdNotFoundContainerListener extends ProcessListener {

	/**
	 * Listener method gets invoked post filtering inactive container
	 * @param input
	 */
	public void onFilteredInactiveContainer(InsIdNotFoundInactiveContainerListenerInput input);
}
