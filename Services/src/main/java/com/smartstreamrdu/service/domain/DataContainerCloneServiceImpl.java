/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerCloneServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	15-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.domain;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmBusinessException;
import com.smartstreamrdu.exception.UdmValidationException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class DataContainerCloneServiceImpl implements DataContainerCloneService{

	
	private static final DataAttribute[] INSTRUMENT_MIN_ATT = new DataAttribute[]{DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), DataAttributeFactory.getDatasourceAttribute(DataLevel.INS)};
	
	private static final DataAttribute[] SECURITY_MIN_ATT = new DataAttribute[]{DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.SEC)};
	
	@Autowired
	private transient CacheDataRetrieval cacheDataRetrieval;


	public void initialize() {
		if (cacheDataRetrieval == null) {
			cacheDataRetrieval = SpringUtil.getBean(CacheDataRetrieval.class);
		}
	}

	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.domain.DataContainerCloneService#createMinimumAttributeInstrumentDC(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public DataContainer createMinimumAttributeInstrumentDC(DataContainer container) throws UdmBaseException {
		if(container == null ){
			return null;
		}
		
		if(container.getLevel() != DataLevel.INS){
			//throw exception
			throw new UdmValidationException(String.format("Requested to craete Instrument DC, however the DC passed is %s",container.getLevel()));
		}
		
		DataContainer insContainer = new DataContainer(DataLevel.INS, container.getDataContainerContext());
		
		addAttributes(insContainer, container, INSTRUMENT_MIN_ATT);
		
		return insContainer;
	}



	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.domain.DataContainerCloneService#createMinimumAttributeSecurityDC(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public DataContainer createMinimumAttributeSecurityDC(DataContainer container) throws UdmBaseException {
		if(container == null ){
			return null;
		}
		
		if(container.getLevel() != DataLevel.SEC){
			//throw exception
			throw new UdmValidationException(String.format("Requested to craete Security DC, however the DC passed is %s",container.getLevel()));
		}
		
		DataContainer secContainer = new DataContainer(DataLevel.SEC, container.getDataContainerContext());
		
		addAttributes(secContainer, container, SECURITY_MIN_ATT);
		
		return secContainer;
	}
	
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.domain.DataContainerCloneService#createMinimumAttributeInactiveInstrumentDC(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public DataContainer createMinimumAttributeInactiveInstrumentDC(DataContainer container) throws UdmBaseException {
		
		DataContainer insContainer = createMinimumAttributeInstrumentDC(container);
		
		if(insContainer == null ){
			return null;
		}
		
		DomainType dataSource = container.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute(container.getLevel()));
		addInactiveFlagToDC(insContainer, dataSource);
		
		return insContainer;

	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.domain.DataContainerCloneService#createMinimumAttributeInactiveSecurityDC(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public DataContainer createMinimumAttributeInactiveSecurityDC(DataContainer container,DomainType dataSource) throws UdmBaseException {
		DataContainer secContainer = createMinimumAttributeSecurityDC(container);
		
		if(secContainer == null ){
			return null;
		}
		
		addInactiveFlagToDC(secContainer,dataSource);
		
		return secContainer;
		
	}

	private void addAttributes(DataContainer newcontainer, DataContainer existingcontainer, DataAttribute[] att) throws UdmBusinessException{
		for(DataAttribute attribute : att){
			DataValue<Serializable> value = new DataValue<>();
			populateLockLevelValues(existingcontainer, attribute, value);
			newcontainer.addAttributeValue(attribute, value);
		}
	}


	/**
	 * @param existingcontainer
	 * @param attribute
	 * @param value
	 * @throws UdmBusinessException
	 */
	private void populateLockLevelValues(DataContainer existingcontainer, DataAttribute attribute,
			DataValue<Serializable> value) throws UdmBusinessException {
		for (LockLevel lockLevel : LockLevel.values()){
			Serializable attributeValueAtLevel = existingcontainer.getAttributeValueAtLevel(lockLevel, attribute );
			if(!attribute.isNull(attributeValueAtLevel)){
				value.setValue(lockLevel, attributeValueAtLevel);
			}
			
		}
	}
	
	private void addInactiveFlagToDC(DataContainer container, DomainType dataSource){
		initialize();
		DataAttribute attribute = getDataAttribute(dataSource,DataAttributeFactory.getStatusFlagForLevel(container.getLevel()));
		DataValue<Serializable> value = new DataValue<>();
		value.setValue(LockLevel.ENRICHED, new DomainType(null, null, DomainStatus.INACTIVE));
		container.addAttributeValue(attribute, value);

	}
	
	private void addActiveFlagToDC(DataContainer container, DomainType dataSource) {
		initialize();	
		DataAttribute attribute = getDataAttribute(dataSource,DataAttributeFactory.getStatusFlagForLevel(container.getLevel()));
	
		DataValue<Serializable> value = new DataValue<>();
		value.setValue(LockLevel.ENRICHED, new DomainType(null, null, DomainStatus.ACTIVE));
		container.addAttributeValue(attribute, value);

	}

   /**
    * This method return dataAttribute for requested container
    * 
    * @param dataSource
    * @param attributeName
    * @return
    */
	private DataAttribute getDataAttribute(DomainType dataSource, String attributeName) {
		DataStorageEnum dataStorage = cacheDataRetrieval.getDataStorageFromDataSource(dataSource.getVal());
		return dataStorage.getAttributeByName(attributeName);
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.domain.DataContainerCloneService#createMinimumAttributeActiveInstrumentDC(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public DataContainer createMinimumAttributeActiveInstrumentDC(DataContainer container) throws UdmBaseException {
		DataContainer insContainer = createMinimumAttributeInstrumentDC(container);
		
		if(insContainer == null ){
			return null;
		}
		DomainType dataSource = container.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute(container.getLevel()));
		addActiveFlagToDC(insContainer,dataSource);
		
		return insContainer;

	}


	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.domain.DataContainerCloneService#createMinimumAttributeActiveSecurityDC(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public DataContainer createMinimumAttributeActiveSecurityDC(DataContainer container,DomainType dataSource) throws UdmBaseException {
		DataContainer secContainer = createMinimumAttributeSecurityDC(container);
		
		if(secContainer == null ){
			return null;
		}
		
		addActiveFlagToDC(secContainer,dataSource);
		
		return secContainer;

	}




}
