/*******************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility
 * All rights reserved.
 *
 * File:    MergingStrategyFactoryImpl.java
 * Author:    Padgaonkar
 * Date:    05-Mar-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.Constant.SdAttributeNames;


/**
 * @author Padgaonkar
 *<br>This facotry will give default merge strategy bean or feed specific bean to do feed specific task.
 *<br> for example. we have SnpXfrInActiveContainerMergingServiceImpl which is used to update only instrument status in db container from feed container.
 *get default strategy or snp strategy for delete
 *
 */
@Component
public class MergingStrategyFactoryImpl implements MergingStrategyFactory{

	private static final String MERGER = "Merger";

	private static final String DEFAULT_MERGER = "DefaultMerger";

	private static final String STATUS = "Status";

	private static final long serialVersionUID = 1L;

    @Autowired
    private NormalizedValueService normalizedService;
   
	@Autowired
	private Map<String, DataContainerMergingService> containerMergingService;

	/**
	 * factory method to give default strategy or feed specific merge strategy 
	 * <br>
	 * for snp we want to extract instrumentStatus from feedContainer and update only this attribute in dbContaier and persist dbContainer.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DataContainerMergingService getMergingStrategy(DataContainer feedContainer, List<DataContainer> dbDataContainers) {
				
		//dataSource	
		DataAttribute dataSource = DataAttributeFactory.getDatasourceAttribute(feedContainer.getLevel());
		DataValue<DomainType> dataSourceValue = (DataValue<DomainType>) feedContainer.getAttributeValue(dataSource);
		
		//status
		DataAttribute statusAttribute = getStatusAttribute(feedContainer.getLevel());
		DomainType currentContainerStatus = (DomainType) feedContainer.getHighestPriorityValue(statusAttribute);
		
		if (currentContainerStatus != null && dataSourceValue != null) {
			
			//If Normalized value is present in container return these value
			Serializable normStatus = currentContainerStatus.getNormalizedValue();
			
			if(normStatus == null) {		
				// get normalized value for status from db
				normStatus = normalizedService.getNormalizedValueForDomainValue(statusAttribute,
						currentContainerStatus, dataSourceValue.getValue().getVal());
			}
			
			// creating Key to find out Factory bean
			String key = new StringBuilder().append(dataSourceValue.getValue().getVal()).append(STATUS).append(normStatus).append(MERGER).toString();

			DataContainerMergingService dataContainerMergingService = containerMergingService.get(key);

			if (dataContainerMergingService != null) {
				return dataContainerMergingService;
			}
		}
		
		return  containerMergingService.get(DEFAULT_MERGER);
		
	}
		
		
		 /**
	     * This method returns status attribute based on input level.
	     * @param level
	     * @return
	     */
	    public DataAttribute getStatusAttribute(DataLevel level) {
	        switch(level) {
	        case INS:
	            return DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_STATUS, level);
	        case LE:
	            return DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.LEGAL_ENTITY_LEGAL_STATUS, level);
	        default:
	            return null;
	        }
	       
	    }

}

