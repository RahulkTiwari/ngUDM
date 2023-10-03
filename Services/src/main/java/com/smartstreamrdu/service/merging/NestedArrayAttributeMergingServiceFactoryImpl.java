/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    NestedArrayAttributeMergingServiceFactoryImpl.java
 * Author:  Dedhia
 * Date:    16-Jul-2020
 *
 ********************************************************************/
package com.smartstreamrdu.service.merging;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.DataSourceConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NestedArrayAttributeMergingServiceFactoryImpl implements NestedArrayAttributeMergingServiceFactory {
	
	private static final String EQUITY_UI_PROGRAME_NAME = "NG-EquityWeb";
	private static final String DEFAULT_NESTED_ARRAY_ATTRIBUTE_MERGING_SERVICE = "DefaultNestedArrayAttributeMergingService";
	private static final String FEED_OVERRIDE_NESTED_ARRAY_ATTRIBUTE_MERGING_SERVICE = "FeedDataOverrideNestedArrayAttributeMergingService";
	
	private static final List<String> FEED_OVERRIDE_DATASOURCES = Arrays.asList(DataSourceConstants.SNP_DS, DataSourceConstants.MOODYS_DS, DataSourceConstants.RDU_ENS,DataSourceConstants.IDC_APEX);
	
	@Autowired
	private Map<String, NestedArrayAttributeMergingService> nestedArrayAttributeMergingServices;
	
	@Override
	public NestedArrayAttributeMergingService getMergingService(DataContainer feedContainer) {
		
		Objects.requireNonNull(feedContainer,"Feed Data container cannot be null.");
		
		String dataSource = getDataSource(feedContainer);
		
		String program = getProgram(feedContainer);
		//In case of edits from UI, only updated array elements will be available in the request
		//Hence use default merging strategy
		if (FEED_OVERRIDE_DATASOURCES.contains(dataSource) && !EQUITY_UI_PROGRAME_NAME.equals(program)) {
			return nestedArrayAttributeMergingServices.get(FEED_OVERRIDE_NESTED_ARRAY_ATTRIBUTE_MERGING_SERVICE);
		} else {
			return nestedArrayAttributeMergingServices.get(DEFAULT_NESTED_ARRAY_ATTRIBUTE_MERGING_SERVICE);
		}
	}


	/**
	 * Returns program from the dataContainer context
	 * @param feedContainer
	 * @return
	 */
	private String getProgram(DataContainer feedContainer) {
		DataContainerContext containerContext = feedContainer.getDataContainerContext();
		String program=null;
		if(containerContext!=null) {
			program = containerContext.getProgram();
		}
		return program;
	}


	/**
	 *  Returns the data source value for the given data container 
	 *  if available.
	 *  
	 * @param dataContainer
	 * @return
	 */
	private String getDataSource(DataContainer dataContainer) {
		DomainType dataSourceValue = null;
		String datasource = Constant.ListenerConstants.dataSource;
		DataLevel rootLevel = dataContainer.getLevel().getRootLevel();
		try {
			DataAttribute attributeByNameAndLevel = DataAttributeFactory.getAttributeByNameAndLevel(datasource,
					rootLevel);
			dataSourceValue = (DomainType) dataContainer.getAttributeValueAtLevel(LockLevel.FEED,
					attributeByNameAndLevel);
		} catch (Exception e) {
			log.warn("DataAttribute is not defined for name {} and level {} ", datasource, rootLevel);
		}
		return dataSourceValue != null ? dataSourceValue.getVal() : null;
	}

}
