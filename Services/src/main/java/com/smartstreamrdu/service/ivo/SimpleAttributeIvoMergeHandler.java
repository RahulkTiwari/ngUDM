/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SimpleAttributeIvoAggregationHandler.java
 * Author : SaJadhav
 * Date : 13-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.util.Constant.ListenerConstants;
import com.smartstreamrdu.util.IvoConstants;

/**
 * @author SaJadhav
 *
 */
@Component
public class SimpleAttributeIvoMergeHandler implements IvoAggregationHandler {
	
	// NOTE: Think about using existing metadata to only merge business attributes
	// which are common between SdData and SdIvo (i.e. based on  Instrument and LE parents)
	private static List<String> technicalAttributes=new ArrayList<>();
	
	static{
		technicalAttributes.add(IvoConstants.CLIENT_ID);
		technicalAttributes.add(IvoConstants.DOC_TYPE);
		technicalAttributes.add(ListenerConstants.updateDate);
		technicalAttributes.add(ListenerConstants.updateUser);
		technicalAttributes.add(ListenerConstants.insertDate);
		technicalAttributes.add(ListenerConstants.insertUser);
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.IvoAggregationHandler#handleAttributeMerge(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataAttribute)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handleAttributeMerge(DataContainer sdContainer, DataContainer sdIvoContainer,
			DataAttribute ivoDataAttribute) {
		if(!technicalAttributes.contains(ivoDataAttribute.getAttributeName())){
			DataLevel normalizedLevel = DataLevel.getNormalizedDataLevelForIvoLevel(ivoDataAttribute.getAttributeLevel());
			DataAttribute sdDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(ivoDataAttribute.getAttributeName(),normalizedLevel);
			
			DataValue<Serializable> sdValue = (DataValue<Serializable>) sdContainer.getAttributeValue(sdDataAttribute);
			if (sdValue == null) {
				// doesn't exist in SdData but only in SdIvo
				// so create one
				sdValue = new DataValue<>();
			}
			
			DataValue<Serializable> sdIvoValue = (DataValue<Serializable>) sdIvoContainer.getAttributeValue(ivoDataAttribute);
			DataValue<?> mergedValue = DataValue.merge(sdValue, sdIvoValue);
			sdContainer.addAttributeValue(sdDataAttribute, mergedValue);
		}
		
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.IvoAggregationHandler#handleAttributeMerge(com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public void handleAttributeMerge(DataRow sdDataRow, DataRow sdIvoDataRow, DataAttribute ivoDataAttribute,
			DataAttribute ivoParentDataAttribute) {
		DataLevel normalizedLevel = DataLevel.getNormalizedDataLevelForIvoLevel(ivoDataAttribute.getAttributeLevel());
		DataAttribute sdDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(ivoDataAttribute.getAttributeName(),normalizedLevel);

		DataValue<Serializable> sdValue = sdDataRow.getAttributeValue(sdDataAttribute);
		DataValue<Serializable> sdIvoValue = sdIvoDataRow.getAttributeValue(ivoDataAttribute);
		if (sdValue != null) {
			DataValue<?> mergedValue = DataValue.merge(sdValue, sdIvoValue);
			sdDataRow.addAttribute(sdDataAttribute, mergedValue);
		} else {
			sdDataRow.addAttribute(sdDataAttribute, sdIvoValue);
		}
	}
	

}
