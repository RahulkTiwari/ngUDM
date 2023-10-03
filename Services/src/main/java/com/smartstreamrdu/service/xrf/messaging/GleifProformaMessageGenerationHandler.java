/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: GleifProformaMessageHandler.java
 * Author: Rushikesh Dedhia
 * Date: October 04, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.messaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.proforma.ProformaMessage;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdAttributeNames;

@Component
public class GleifProformaMessageGenerationHandler implements ProformaMessageGenerationHandler {

	private static final Logger _logger = LoggerFactory.getLogger(GleifProformaMessageGenerationHandler.class);

	private static final DataAttribute LEGAL_ENTITY_RELATION_ATTTRIBUTE = DataAttributeFactory.getRelationAttributeForLe();
	private static final DataAttribute LEGAL_ENTITY_STATUS = DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.LEGAL_ENTITY_STATUS, DataLevel.LE);
	private static final DataAttribute REF_DATA_ATTRIBUTE = DataAttributeFactory.getRelationRefDataAttribute(LEGAL_ENTITY_RELATION_ATTTRIBUTE);
	private static final String ULTIMATE_PARENT = "Ultimate Parent";
	private static final String IMMEDIATE_PARENT = "Immediate Parent";
	private static final DataAttribute LEI_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.LEI, DataLevel.LE);
	private static final DataAttribute RELATION_TYPE = DataAttributeFactory.getAttributeByNameAndLevelAndParent(SdAttributeNames.RELATION_TYPE, DataLevel.LE, LEGAL_ENTITY_RELATION_ATTTRIBUTE);

	@Autowired
	private DataRetrievalService dataRetrievalService;
	
	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;


	private List<DataContainer> getChildLeiContainers(DataContainer postChangeDataContainer)  {
		Criteria criteria = null;
		
		try {
			criteria  = createCriteriaForChildLeiContainers(postChangeDataContainer);

			DataRetrivalInput input = new DataRetrivalInput();
			input.setCriteria(criteria);
			input.setLevel(DataLevel.LE);
		
			return dataRetrievalService.retrieve(Database.Mongodb, input);
		} catch (UdmTechnicalException e) {
			_logger.error("Following error occured while fetching data for the criteria : {} from the database.", criteria, e);
		}

		return Collections.emptyList();
	}

	/**
	 *  Creates and returns a criteria that has the conditions for the lei to be the Immediate and Ultimate parents.
	 * @param postChangeDataContainer
	 * @return
	 * @throws UdmTechnicalException 
	 */
	private Criteria createCriteriaForChildLeiContainers(DataContainer postChangeDataContainer) throws UdmTechnicalException {

		String lei = (String) ProformaMessageGeneratorUtil.getValueForDataAttribute(LEI_ATTRIBUTE, postChangeDataContainer);


		Criteria relationCriteria = getCriteriaForRelationType(lei, ULTIMATE_PARENT).orOperator(getCriteriaForRelationType(lei, IMMEDIATE_PARENT));
		
		return getStatusActiveCriteria().andOperator(relationCriteria);
	}

	/**
	 *  Returns a criteria object which has a combination of the relation type and the LEI value.
	 * @param lei
	 * @param relationTypeValue
	 * @return
	 */
	private Criteria getCriteriaForRelationType(String lei, String relationTypeValue) {

		// Create data attribute and value for relationType.
		DataValue<String> relationTypeDataValue = new DataValue<>();
		relationTypeDataValue.setValue(LockLevel.FEED, relationTypeValue);

		// Create data row for ref-data.
		DataRow refData = new DataRow(REF_DATA_ATTRIBUTE);
		// Create data attribute and value for lei attribute.
		DataValue<String> leiDataValue = new DataValue<>();
		leiDataValue.setValue(LockLevel.FEED, lei);
		// Add lei data value to refData data row.
		refData.addAttribute(LEI_ATTRIBUTE, leiDataValue);


		ArrayList<DataRow> refDataList = new ArrayList<>();
		refDataList.add(refData);
		
		Criteria relationCriteria = Criteria.where(RELATION_TYPE).is(relationTypeDataValue);

		Criteria refDataCriteria = Criteria.where(REF_DATA_ATTRIBUTE).is(refData);
		
		return relationCriteria.andOperator(refDataCriteria);
	}

	/**
	 *  This method returns a criteria for the legalEntityStatus to be active.
	 * @return
	 * @throws UdmTechnicalException 
	 */
	private Criteria getStatusActiveCriteria() throws UdmTechnicalException {

		List<DataValue<? extends Serializable>> listDataValues = new ArrayList<>();
		String rduDomain = DataAttributeFactory.getRduDomainForDomainDataAttribute(LEGAL_ENTITY_STATUS);
		String normalizedValue = DomainStatus.ACTIVE;
		String domainSource = "gleif";

		List<DomainType> domainValues = cacheDataRetrieval.getVendorDomainValuesFromCache(rduDomain, domainSource,
				normalizedValue);
		if (CollectionUtils.isNotEmpty(domainValues)) {
			for (Serializable vendorVal : domainValues) {
				DataValue<Serializable> dataValue = new DataValue<>();
				dataValue.setValue(LockLevel.FEED, vendorVal);
				listDataValues.add(dataValue);
			}
		} else {
			throw new UdmTechnicalException("no vendor domain value returned for this gleif event for active statuses : "+ domainValues, null);
		}
		return Criteria.where(LEGAL_ENTITY_STATUS).in(listDataValues);
	}

	@Override
	public List<ProformaMessage> createProformaMessages(DataContainer dataContainer) {

		Objects.requireNonNull(dataContainer, "DataContainer cannot be null.");

		List<ProformaMessage> proformaMessages = new ArrayList<>();

		if (dataContainer.getLevel() != DataLevel.LE) {
			throw new IllegalArgumentException("Incorrect data level found for GLEIF data container. DataLevel is : "+dataContainer.getLevel());
		} 
		
		// Create and add the message for the original document.
		proformaMessages.add(ProformaMessageGeneratorUtil.createProformaMessage(dataContainer, DataLevel.LE));
		// Fetch the child LEIs of the incoming LEI.
		List<DataContainer> childLeiContainers = getChildLeiContainers(dataContainer);
		// Create a message for each of the child and add to the list.
		for(DataContainer childLeiContainer : childLeiContainers) {
			proformaMessages.add(ProformaMessageGeneratorUtil.createProformaMessage(childLeiContainer, DataLevel.LE));
		}
		return proformaMessages;

	}

}
