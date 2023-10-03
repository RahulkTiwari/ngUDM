/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	StatusServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	12-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.status;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class StatusServiceImpl implements StatusService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1868958734568460780L;

	@Autowired
	private NormalizedValueService normalizedService;
	
	@Autowired
	private CacheDataRetrieval cacheDataRetrieve;

	private void init(){
		if(normalizedService == null){
			normalizedService = SpringUtil.getBean(NormalizedValueService.class);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.status.StatusService#isStatusInactive(com.
	 * smartstreamrdu.domain.DataContainer, java.lang.String)
	 */
	@Override
	public boolean isStatusInactive(DataContainer container, String datasource) {
		if (container == null || datasource == null) {
			return false;
		}
		init();
		DataStorageEnum dataStorage = cacheDataRetrieve.getDataStorageFromDataSource(datasource);
		DataAttribute statusAtt = dataStorage
				.getAttributeByName((DataAttributeFactory.getStatusFlagForLevel(container.getLevel())));

		Serializable attributeValue2 = container.getAttributeValue(statusAtt);
		if (attributeValue2 instanceof DataValue) {
			DataValue<? extends Serializable> val = (DataValue<? extends Serializable>) attributeValue2;

			Serializable attributeValue = val.getValue();

			if (attributeValue instanceof DomainType) {
				DomainType dt = (DomainType) attributeValue;
				return DomainStatus.INACTIVE
						.equals(normalizedService.getNormalizedValueForDomainValue(statusAtt, dt, datasource));
			}

		}

		return false;
	}

}
