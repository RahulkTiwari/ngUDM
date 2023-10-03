/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataProcessListener.java
 * Author:	Padgaonkar S
 * Date:	29-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata.listeners;

import com.smartstreamrdu.service.event.process.listener.ProcessListener;

/**
 * Generic interface to process rawData events
 * @author Padgaonkar
 *
 */
public interface RawDataProcessListener extends ProcessListener {

	/**
	 * invoking events when raw record is getting filtered.
	 * @param input
	 */
	public void onRawRecordFiltered(RawDataListenerInput input);

}
