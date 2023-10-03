/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : VfsOpenFigiEligibleAttributesService.java
 * Author :SaJadhav
 * Date : 30-Nov-2021
 */
package com.smartstreamrdu.service.openfigi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.xrf.XrfRuleAttributeDef;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.service.xrf.config.CrossRefRuleConfigService;
import com.smartstreamrdu.util.UdmSystemPropertiesConstant;

import lombok.Setter;

/**
 * Service for getting attributes eligible for sending message to VFs open figi
 * 
 * @author SaJadhav
 *
 */
@Component
public class VfsOpenFigiEligibleAttributesService {
	
	@Setter
	@Autowired
	private UdmSystemPropertiesCache udmSystemPropertiescache;
	
	@Setter
	@Autowired
	private CrossRefRuleConfigService crossRefRuleConfigService;
	
	private  List<DataAttribute> xrfAttributesList = new ArrayList<>();
	
	//List of non-xrf attributes to be considered for triggering VFS request
	private  List<DataAttribute> nonXrfAttributesList = new ArrayList<>();
	
	//List of non-xrf attributes at IVO_INS level to be considered for triggering VFS request in case of centralized IVO locks 
	private List<DataAttribute> nonXrfIvoInsAttributesList = new ArrayList<>();
	
	//List of non-xrf attributes at IVO_SEC level to be considered for triggering VFS request in case of centralized IVO locks
	private List<DataAttribute> nonXrfIvoSecAttributesList = new ArrayList<>();
	
	@PostConstruct
	public void initializeVfsEligibleAttributes() {
		initializeXrfAttributeList();
		initializeNonXrfAttributeList();
	}

	/**
	 * Fetches the property value nonXrfAttributesForVfsRequest from udmSytemProerties and initialises
	 * nonXrfAttributesList
	 * 
	 * @throws UdmTechnicalException
	 */
	private void initializeNonXrfAttributeList() {

		Optional<List<String>> nonXrfAttributesValue = udmSystemPropertiescache.getPropertiesValues(
				UdmSystemPropertiesConstant.NON_XRF_ATTRIBUTES_FOR_VFS_REQUEST, DataLevel.UDM_SYSTEM_PROPERTIES);
		if (nonXrfAttributesValue.isPresent()) {
			List<String> listNonXrfAttributes = nonXrfAttributesValue.get();
			List<DataAttribute> nonXrfAttributesForVfsrequest = listNonXrfAttributes.stream()
					.map(DataStorageEnum.SD::getAttributeByName).collect(Collectors.toList());
			this.nonXrfAttributesList.addAll(nonXrfAttributesForVfsrequest);
			nonXrfAttributesForVfsrequest.forEach(this::initializeNonXrfIvoAttributeLists);
		}
	}

	/**
	 * @param attribute
	 * @return
	 */
	private void initializeNonXrfIvoAttributeLists(DataAttribute attribute) {
		DataAttribute ivoDataAttribute=DataAttributeFactory.getAttributeByNameAndLevel(attribute.getAttributeName(),
				DataLevel.getIvoDataLevelForNormalizedDataLevel(attribute.getAttributeLevel()));
		if(attribute.getAttributeLevel().equals(DataLevel.INS)) {
			this.nonXrfIvoInsAttributesList.add(ivoDataAttribute);
		}else {
			this.nonXrfIvoSecAttributesList.add(ivoDataAttribute);
		}
	}

	/**
	 * 
	 */
	private void initializeXrfAttributeList() {
		List<XrfRuleAttributeDef> xrfAttributes = crossRefRuleConfigService.getMandatoryAndOptionalCrossRefAttributes();
		this.xrfAttributesList = xrfAttributes.stream()
				.map(xrfAttribute -> DataAttributeFactory.getAttributeByNameAndLevel(xrfAttribute.getSdAttributeName(),
						DataLevel.valueOf(xrfAttribute.getSdAttributeLevel())))
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns non xrf attributes eligible for sending VFS OpenFigi request
	 * @return
	 */
	public List<DataAttribute> getNonXrfAttributesEligibleForVfsRequest() {
		return Collections.unmodifiableList(this.nonXrfAttributesList);
	}
	
	/**
	 *  Returns xrf attributes eligible for sending VFS OpenFigi request
	 * 
	 * @return
	 */
	public List<DataAttribute> getXrfAttributesEligibleForVfsRequest(){
		return Collections.unmodifiableList(this.xrfAttributesList);
	}
	
	/**
	 *  Returns non xrf attributes at IVO_INS level eligible for sending VFS OpenFigi request
	 * @return
	 */
	public List<DataAttribute> getNonXrfIvoInsAttributesEligibleForVfsRequest(){
		return Collections.unmodifiableList(this.nonXrfIvoInsAttributesList);
	}
	
	/**
	 *  Returns non xrf attributes at IVO_SEC level eligible for sending VFS OpenFigi request
	 * @return
	 */
	public List<DataAttribute> getNonXrfIvoSecAttributesEligibleForVfsRequest(){
		return Collections.unmodifiableList(this.nonXrfIvoSecAttributesList);
	}
	
}
