/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RduRuleProcessListener.java
 * Author:	Padgaonkar S
 * Date:	29-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules.listeners;

import com.smartstreamrdu.service.event.process.listener.ProcessListener;

/**
 * Generic listener interface to invoke rduRuleListeners
 * @author Padgaonkar
 *
 */
public interface RduRuleProcessListener extends ProcessListener {

	public void onRuleExecution(RduRuleListenerInput input);

}
