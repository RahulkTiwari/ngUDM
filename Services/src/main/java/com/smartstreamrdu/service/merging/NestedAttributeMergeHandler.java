/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: NestedAttributeMergeHandler.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;

/**
 * @author Dedhia
 *
 */
@Component
public class NestedAttributeMergeHandler implements AttributeMergeHandler {


	@Autowired
	AttributeMergeHandlerFactoryImpl mergeHandlerFactory;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.AttributeMergeHandler#handleAttributemerge(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public void handleAttributeMerge(DataContainer feedContainer, DataContainer dbContainer, DataAttribute dataAttribute) {
		
		DataRow feedDataRow = (DataRow) feedContainer.getAttributeValue(dataAttribute);
		DataRow dbDataRow = (DataRow) dbContainer.getAttributeValue(dataAttribute);
		
		if (dbDataRow == null) {
			dbContainer.addAttributeValue(dataAttribute, feedDataRow);
			return;
		}
		
		String dataSource = feedContainer.getDataContainerContext().getDataSource();
		handleAttributeMerge(feedDataRow, dbDataRow, dataAttribute,new DomainType(dataSource));
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.AttributeMergeHandler#handleAttributeMerge(com.smartstreamrdu.domain.DataValue, com.smartstreamrdu.domain.DataValue, com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public void handleAttributeMerge(DataRow feedDataRow, DataRow dbDataRow, DataAttribute dataAttribute,DomainType dataSource) {
		Map<DataAttribute, DataValue<Serializable>> feedRowData = feedDataRow.getRowData();
		Set<DataAttribute> dataAttributes = feedRowData.keySet();
		for (DataAttribute attribute : dataAttributes) {
			AttributeMergeHandler mergeHandler = mergeHandlerFactory.getMergeHandler(attribute);
			
			Serializable feedattributeValue = feedDataRow.getAttributeValue(attribute);
			
			Serializable dbAttributeValue = dbDataRow.getAttributeValue(attribute);
			
			if(feedattributeValue instanceof DataRow){
				DataRow feedRow = (DataRow)feedattributeValue;
				DataRow dbRow = dbAttributeValue instanceof DataRow ? (DataRow)dbAttributeValue : null;
				if(dbRow == null){
					dbDataRow.addAttribute(attribute, feedRow);
				}
				else if(DataType.NESTED.equals(attribute.getDataType())){
					mergeHandler.handleAttributeMerge(feedRow, dbRow, attribute,dataSource);
				}
				else {
					mergeHandler.handleAttributeMerge(feedDataRow, dbDataRow, attribute,dataSource);
				}
			}
			else{
				mergeHandler.handleAttributeMerge(feedDataRow, dbDataRow, attribute,dataSource);
			}
		}
	}

}
