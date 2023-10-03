/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainSearchService.java
 * Author : VRamani
 * Date : Jan 21, 2020
 * 
 */
package com.smartstreamrdu.service.domain;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;

/**
 * Service to search Domain Mappings for corresponding RDU Domain
 * 
 * @author VRamani
 *
 */
public interface DomainSearchService {

	/**
	 * Finds the DvDomainMap DataContainer for the corresponding RDU Domain
	 * DataContainer. Search will be based on the primary key attribute of the
	 * domain container which will be Normalized Value in DvDomainMap
	 * 
	 * @param domainDataContainer
	 * @return dvDomainDataContainer
	 */
	DataContainer findVendorMappingsFromRduDomain(DataContainer domainDataContainer);
	
	/**
	 * get rduDomain collectionname from data container
	 * @param level
	 * @return
	 */
	String getRduDomainFromDataLevel(DataLevel level);
}
