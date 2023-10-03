/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    InsInactivationLevelBasedDataContainerRejectionService.java
 * Author:  Padgaonkar
 * Date:    March 01, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.persist;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdAttributeNames;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * This service is removes dataContainers from persistence list based on
 * following Logic. If dataSource.level is INS & dataSource.insInactivationLevel
 * is SEC.(e.g.trdse dataSource) In this case for new insert if container
 * consist of only active technical security then it restrict it from
 * persistence.
 * 
 * @author Padgaonkar
 *
 */
@Component
@Slf4j
public class InsInactivationLevelBasedDataContainerRejectionService implements DataContainerRejectionService {

	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;

	@Override
	public void validateAndRemoveInvalidContainerFromList(List<DataContainer> dataContainers) {
		Iterator<DataContainer> iterator = dataContainers.iterator();
		removeDataContainerBasedOnInsInactivationLevel(iterator);
	}

	/**
	 * This service is removes dataContainers from persistence list based on
	 * following Logic. If dataSource.level is INS & dataSource.insInactivationLevel
	 * is SEC.(e.g.trdse dataSource) In this case for new insert if container
	 * consist of only active technical security then it restrict it from
	 * persistence.
	 * 
	 * @param iterator
	 */

	private void removeDataContainerBasedOnInsInactivationLevel(Iterator<DataContainer> iterator) {
		while (iterator.hasNext()) {
			DataContainer container = iterator.next();
			if (isApplicable(container)) {
				DataAttribute dataSource = DataAttributeFactory
						.getAttributeByNameAndLevel(SdAttributeNames.DATASOURCE_ATTRIBUTE, DataLevel.Document);
				DomainType dataSourceDomain = container.getHighestPriorityValue(dataSource);

				if (Objects.isNull(dataSourceDomain) || Objects.isNull(dataSourceDomain.getVal())) {
					return;
				}
				String dataSourceVal = dataSourceDomain.getVal();
				String dataSourceInsInactivationLevel = cacheDataRetrieval
						.getDataSourceInsInactivationLevel(dataSourceVal);
				String dataSourceLevel = cacheDataRetrieval.getDataSourceLevelFromCode(dataSourceVal);

				if (SdDataAttConstant.INSTRUMENT.equals(dataSourceLevel)
						&& SdDataAttConstant.SECURITY.equals(dataSourceInsInactivationLevel)
						&& containsOnlyActiveTechnicalSecurity(container)) {
					log.info(
							"Following dataContainer:{} is removed from persistenceList as it contains only active technical security",
							container);
					iterator.remove();
				}
			}
		}
	}

	/**
	 * This method returns true if container contains only active technical
	 * security.
	 * 
	 * @param container
	 * @param dataSourceVal
	 * @return
	 */
	private boolean containsOnlyActiveTechnicalSecurity(DataContainer container) {
		List<DataContainer> allChildDataContainers = container.getAllChildDataContainers();

		if (CollectionUtils.isEmpty(allChildDataContainers) || allChildDataContainers.size() > 1) {
			return false;
		}
		DataContainer childContainer = allChildDataContainers.get(0);
		DomainType rduSecType = childContainer.getHighestPriorityValue(SdDataAttributeConstant.RDU_SEC_TYPE);
		String securityStatus = getTechnicalSecurityStatus(SdDataAttributeConstant.SEC_STATUS,
				childContainer);

		return ((rduSecType != null && SdDataAttConstant.TECHNICAL.equals(rduSecType.getNormalizedValue()))
				&& DomainStatus.ACTIVE.equals(securityStatus));
	}

	/**
	 * This method validates whether container is applicable or not.
	 * 
	 * @param container
	 * @return
	 */
	private boolean isApplicable(DataContainer container) {
		return container.isNew() && container.getLevel() != null && container.getLevel().equals(DataLevel.INS);

	}

	/**
	 * This method returns security status value for technical Security.
	 * 
	 * @param securityStatus
	 * @param dataSource
	 * @param childContainer
	 * @return
	 */
	private String getTechnicalSecurityStatus(DataAttribute securityStatus, DataContainer childContainer) {
		DomainType secStatus = childContainer.getHighestPriorityValue(securityStatus);

		if (secStatus.getNormalizedValue() != null) {
			return secStatus.getNormalizedValue();
		}
		return null;
	}

}
