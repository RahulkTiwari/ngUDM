/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainSearchServiceImple.java
 * Author : VRamani
 * Date : Jan 21, 2020
 * 
 */
package com.smartstreamrdu.service.domain;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainMaintenanceMetadata;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.repository.service.DomainMetadataRepositoryService;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;

/**
 * @author VRamani
 *
 */
@Component
public class DomainSearchServiceImpl implements DomainSearchService {

	private static final Logger _logger = LoggerFactory.getLogger(DomainSearchServiceImpl.class);

	private static final DataAttribute RDU_DOMAIN_ATTRIBUTE = DataAttributeFactory
			.getAttributeByNameAndLevel("rduDomain", DataLevel.DV_DOMAIN_MAP);
	private static final DataAttribute NORMALIZED_VALUE_ATTRIBUTE = DataAttributeFactory
			.getAttributeByNameAndLevel("normalizedValue", DataLevel.DV_DOMAIN_MAP);

	@Autowired
	private DomainMetadataRepositoryService metaDataRepoService;

	@Autowired
	private DataRetrievalService retrievalService;

	@Override
	public DataContainer findVendorMappingsFromRduDomain(DataContainer rduDomainContainer) {
		String rduDomain = getRduDomainFromDataLevel(rduDomainContainer.getLevel());
		String primaryKey = getPrimaryKeyName(rduDomain);
		DataAttribute primaryKeyAttribute = DataAttributeFactory.getAttributeByNameAndLevel(primaryKey,
				rduDomainContainer.getLevel());
		String primaryKeyValue = rduDomainContainer.getHighestPriorityValue(primaryKeyAttribute);

		if (StringUtils.isEmpty(primaryKeyValue)) {
			return null;
		}
		DataValue<String> rduDomainVal = new DataValue<>();
		rduDomainVal.setValue(LockLevel.RDU, rduDomain);
		Criteria rduDomainCriteria = Criteria.where(RDU_DOMAIN_ATTRIBUTE).is(rduDomainVal);

		DataValue<String> normalizedValue = new DataValue<>();
		normalizedValue.setValue(LockLevel.RDU, primaryKeyValue);
		Criteria normalizedValCriteria = Criteria.where(NORMALIZED_VALUE_ATTRIBUTE).is(normalizedValue);

		DataRetrivalInput input = new DataRetrivalInput();
		input.setCriteria(new Criteria().andOperator(rduDomainCriteria, normalizedValCriteria));

		try {
			List<DataContainer> retrievedDomainMaps = retrievalService.retrieve(Database.Mongodb, input);
			if (!CollectionUtils.isEmpty(retrievedDomainMaps)) {
				// only one Domain mapping document will be present for rduDomain and
				// normalizedValue combination
				return retrievedDomainMaps.get(0);
			}
			_logger.info("No domain mappings found for rduDomain {} and normalizedValue {}", rduDomain,
					primaryKeyValue);
		} catch (UdmTechnicalException e) {
			_logger.info("No domain mappings found for rduDomain {} and normalizedValue {}", rduDomain,
					primaryKeyValue);
		}
		return null;
	}

	/**
	 * Search domainMaintenanceMetadata collection using rduDomain and return primaryKey column name. 
	 * @param rduDomain
	 */
	private String getPrimaryKeyName(String rduDomain) {
		DomainMaintenanceMetadata domainMetadata = metaDataRepoService.getDomainMetadataMap().get(rduDomain);
		return domainMetadata.getPrimaryKey();
	}

	/**
	 * @param level
	 * @return
	 */
	@Override
	public String getRduDomainFromDataLevel(DataLevel level) {
		String simpleName = level.getCollectionName();
		return StringUtils.isNotEmpty(simpleName)
				? StringUtils.lowerCase(simpleName.substring(0, 1)) + simpleName.substring(1, simpleName.length())
				: "";
	}

}
