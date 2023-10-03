/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerTestUtil.java
 * Author:	Ravi Shah
 * Date:	31-May-2018
 *
 *******************************************************************
 */

package com.smartstreamrdu.domain;

/**
 * A utility class to create and return empty DataContainer objects.
 *
 */
public class DataContainerTestUtil {

	public static DataContainer getLegalEntityContainer() {
		return new DataContainer(DataLevel.LE, getNewContext());
	}

	public static DataContainer getLegalEntityContainer(DataContainerContext context) {
		return new DataContainer(DataLevel.LE, context);
	}

	public static DataContainer getInstrumentContainer() {
		return new DataContainer(DataLevel.INS, getNewContext());
	}

	public static DataContainer getInstrumentContainer(DataContainerContext context) {
		return new DataContainer(DataLevel.INS, context);
	}

	public static DataContainer getSecurityContainer() {
		return new DataContainer(DataLevel.SEC, getNewContext());
	}

	public static DataContainer getSecurityContainer(DataContainerContext context) {
		return new DataContainer(DataLevel.SEC, context);
	}

	public static DataContainer getDataContainer(DataLevel level) {
		return new DataContainer(level, getNewContext());
	}

	private static DataContainerContext getNewContext() {
		return DataContainerContext.builder().build();
	}
	
}
