/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LookupRelationship.java
 * Author:	Jay Sangoi
 * Date:	17-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.lookup;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.service.lookup.input.LookupAttributeInput;

/**
 * @author Jay Sangoi
 *
 */
public interface LookupRelationship {

	/**
	 * Performs the lookup using LookupAttributeINput. If lookup found, then it will update the DataContainer reference Id
	 * @param feedContainer - Container created by feed
	 * @param input - Lookup attribute Input
	 * @throws UdmBaseException 
	 */
	void resolveLookup(DataContainer feedContainer, LookupAttributeInput input) throws UdmBaseException;
	
}
