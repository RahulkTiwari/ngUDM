/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedLookupService.java
 * Author:	Jay Sangoi
 * Date:	19-Apr-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.lookup;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.service.lookup.input.LookupAttributeInput;

/**
 * Service for performing lookup and populating reference id if available.
 * @author Jay Sangoi
 *
 */
public interface LookupService extends Serializable{
	
	/**
	 * Performs the lookup using LookupAttributeINput. If lookup found, then it will update the DataContainer reference Id
	 * @param feedContainer - Container created by feed
	 * @param input - Lookup attribute Input
	 * @throws UdmBaseException 
	 */
	void resolveLookup(DataContainer feedContainer, LookupAttributeInput input) throws UdmBaseException;

	/**
	 * Get the db container(s) equivalent to feed container by SourceUniqueId. 
	 * <p><b>This does check the status of the db container only at FEED lock level.</b></p>
	 * 
	 * @param feedContainer
	 * @param feedDs
	 * @return
	 * @throws UdmBaseException
	 */
	List<DataContainer> getDbDataContainersBySourceUniqueId(DataContainer feedContainer, DomainType feedDs) throws UdmBaseException;

	/**
	 * Filters the ACTIVE dataContainer(s) returned from {@link #getDbDataContainersBySourceUniqueId(DataContainer, DomainType)}
	 * 
	 * @param feedContainer
	 * @param feedDs
	 * @return
	 * @throws UdmBaseException
	 */
	List<DataContainer> getActiveDbDataContainersBySourceUniqueId(DataContainer feedContainer, DomainType feedDs) throws UdmBaseException;

	
	/**Populate the datasource
	 * @param container
	 * @param datasource
	 */
	void populateDataSource(DataContainer container, String datasource);
	
	
	/**
	 * 
	 * Filters the ACTIVE dataContainer(s) returned from {@link #getDbInstrumentContainerForSecurity(DomainType, List)}
	 * 
	 * @param datasource
	 * @param securityContainers
	 * @return
	 * @throws UdmBaseException
	 */
	List<DataContainer> getActiveDbInstrumentContainerForSecurity(DomainType datasource , List<DataContainer> securityContainers) throws UdmBaseException;
	
	/**
	 * Filters the ACTIVE dataContainer based on objectId
	 * 
	 * @param objectId
	 * @return
	 * @throws UdmTechnicalException
	 */
	List<DataContainer> getDbDataContainersByObjectId(String objectId, DataLevel level) throws UdmTechnicalException;
}
