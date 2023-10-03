/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: NestedArrayAttributeIvoAggregationHandler.java
 * Author : SaJadhav
 * Date : 14-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;

/**
 * @author SaJadhav
 *
 */
@Component
public class NestedArrayAttributeIvoAggregationHandler implements IvoAggregationHandler {
	
	@Autowired
	private SdIvoToSdNestedArrayComparator nestedArrayComparator;
	
	@Autowired
	private NestedAttributeIvoAggregationHandler nestedHandler;
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.IvoAggregationHandler#handleAttributeMerge(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public void handleAttributeMerge(DataContainer sdContainer, DataContainer sdIvoContainer,
			DataAttribute ivoDataAttribute) {

		DataLevel normalizedLevel = DataLevel.getNormalizedDataLevelForIvoLevel(ivoDataAttribute.getAttributeLevel());
		DataAttribute sdDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(ivoDataAttribute.getAttributeName(),normalizedLevel);
		DataRowIterator ivoRowIterator = new DataRowIterator(sdIvoContainer, ivoDataAttribute);
		
		
		while(ivoRowIterator.hasNext()){
			DataRow row = ivoRowIterator.next();
			DataRowIterator sdRowIterator = new DataRowIterator(sdContainer, sdDataAttribute);
			DataRow sdRow = nestedArrayComparator.compareAndReturn(ivoDataAttribute, row, sdRowIterator);
			if(sdRow!=null){
				//perform nested merging
				nestedHandler.handleAttributeMerge(sdRow, row, null,ivoDataAttribute);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.IvoAggregationHandler#handleAttributeMerge(com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public void handleAttributeMerge(DataRow sdDataRow, DataRow sdIvoDataRoow, DataAttribute dataAttribute,DataAttribute ivoParentDataAttribute) {
		throw new UnsupportedOperationException(String.format("NestedArrayAttributeIvoAggregationHandler->%s","handleAttributeMerge with data row"));

	}

}
