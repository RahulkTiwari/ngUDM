/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility
 * All rights reserved.
 *
 * File:    XrfSourcePriorityEvaluator.java
 * Author:    Ashok Thanage
 * Date:    07-Jan-2020
 */
package com.smartstreamrdu.service.events;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.persistence.domain.autoconstants.DataSourcesAttrConstant;
import com.smartstreamrdu.service.staticdata.StaticDataServiceImpl;

/**
 * This class checks if data container is eligible for sending to xrf.
 * DataSources having negative source priority should not be send to xrf.
 * @author Ashok Thanage
 */

@Component
public class XrfEligibilityEvaluator {
	
	@Autowired
	private StaticDataServiceImpl dataService;
	
	private static final int ZERO = 0;
	
	/**
	 * Returns true if dataContainer is eligible for sending to XRF
	 * 
	 * @param dataContainer
	 * @return
	 * @throws UdmBaseException
	 * com.smartstreamrdu.service.events
	 */
	public boolean isDataContainerEligibleForXrf(DataContainer dataContainer) throws UdmBaseException {
		Objects.requireNonNull(dataContainer, "DataContainer cannot be null.");

		DataAttribute dataSourceAttribute = DataAttributeFactory.getDatasourceAttribute(dataContainer.getLevel());

		DomainType dataSource = dataContainer.getHighestPriorityValue(dataSourceAttribute);
		// dataSource will not be present in IVO data containers
		if (dataSource == null) {
			return false;
		}

		int xrfPriorityVal = Integer.parseInt((String) dataService.getDataByCode(DataLevel.DATA_SOURCES,
				DataSourcesAttrConstant.XRF_SOURCE_PRIORITY, dataSource.getVal()));
		return (xrfPriorityVal > ZERO);
	}
}
