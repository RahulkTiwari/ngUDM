/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	AbstractDataContainerPostProcessorService.java
 * Author:	Rushikesh Dedhia
 * Date:	14-June-2019
 *
 ********************************************************************/
package com.smartstreamrdu.service.postprocess;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.util.SdDataAttributeConstant;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;

@Component
public abstract class AbstractDataContainerPostProcessorService implements DataContainerPostProcessorService {
	
	private static final Logger _logger = LoggerFactory.getLogger(AbstractDataContainerPostProcessorService.class);

	@Override
	public final void postProcess(String dataSource, List<DataContainer> dataContainers)
			throws UdmTechnicalException {
		
		if (isServiceApplicable(dataSource, dataContainers)) {
			_logger.debug("Applying post processor <--- {} ---> for dataSource : {}, and dataContainers : {}."
					, this.getClass().getName(), dataSource, dataContainers);
			postProcessContainers(dataSource, dataContainers);
		}
	}
	
	public abstract boolean isServiceApplicable(String dataSource, List<DataContainer> dataContainers) throws UdmTechnicalException;
	
	public abstract void postProcessContainers(String dataSource, List<DataContainer> dataContainers) throws UdmTechnicalException;


	/**
	 * This methods returns whether requested security is Technical or not. 
	 * @param container
	 * @return
	 */
	protected boolean isTechnicalSecurity(DataContainer container) {
		DomainType secType = (DomainType) container.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
		if (secType != null) {
			String secTypeVal = secType.getNormalizedValue();
			return StringUtils.isNotEmpty(secTypeVal) && secTypeVal.equals(SdDataAttConstant.TECHNICAL);
		}
		return false;
	}
}
