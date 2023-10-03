/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: NestedAttributeIvoAggregationHandler.java
 * Author : SaJadhav
 * Date : 14-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.util.Constant.SdAttributeNames;

/**
 * @author SaJadhav
 *
 */
@Component
public class NestedAttributeIvoAggregationHandler implements IvoAggregationHandler {
	
	//List of technical attributes which need not be merged from sdIvo to Sd
	private static List<String> technicalAttributes=new ArrayList<>();
	
	static {
		technicalAttributes.add(SdAttributeNames.RELATION_TYPE);
		technicalAttributes.add(SdAttributeNames.REFERENCEID);
	}
	
	@Autowired
	private IvoAggregationHandlerFactory aggregationHandlerFactory;
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.IvoAggregationHandler#handleAttributeMerge(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public void handleAttributeMerge(DataContainer sdContainer, DataContainer sdIvoContainer,
			DataAttribute ivoDataAttribute) {
		
		DataLevel normalizedLevel = DataLevel.getNormalizedDataLevelForIvoLevel(ivoDataAttribute.getAttributeLevel());
		DataAttribute sdDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel(ivoDataAttribute.getAttributeName(),normalizedLevel);
		
		DataRow sdIvoDataRow = (DataRow) sdIvoContainer.getAttributeValue(ivoDataAttribute);
		DataRow sdDataRow = (DataRow) sdContainer.getAttributeValue(sdDataAttribute);
		
		if (sdDataRow!=null) {
			handleAttributeMerge(sdDataRow, sdIvoDataRow, ivoDataAttribute,null);
		}
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.IvoAggregationHandler#handleAttributeMerge(com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataRow, com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public void handleAttributeMerge(DataRow sdDataRow, DataRow sdIvoDataRow, DataAttribute ivoDataAttribute,
			DataAttribute ivoParentDataAttribute) {

		Map<DataAttribute, DataValue<Serializable>> ivoRowData = sdIvoDataRow.getRowData();
		Set<DataAttribute> dataAttributes = ivoRowData.keySet();
		for (DataAttribute attribute : dataAttributes) {
			if (technicalAttributes.contains(attribute.getAttributeName())) {
				continue;
			}
			IvoAggregationHandler mergeHandler = aggregationHandlerFactory.getIvoAggregationHandler(attribute);

			Serializable ivoAttributeValue = sdIvoDataRow.getAttributeValue(attribute);
			if (ivoAttributeValue instanceof DataRow) {
				DataRow ivoRow = (DataRow) ivoAttributeValue;
				DataRow sdRow = getSdAttributeValue(sdDataRow, attribute, ivoParentDataAttribute);
				if (sdRow != null) {
					mergeHandler.handleAttributeMerge(sdRow, ivoRow, attribute, null);
				}
			} else {
				mergeHandler.handleAttributeMerge(sdDataRow, sdIvoDataRow, attribute, null);
			}
		}

	}

	/**
	 * @param sdDataRow
	 * @param attribute
	 * @param ivoParentDataAttribute
	 * @return
	 */
	private DataRow getSdAttributeValue(DataRow sdDataRow, DataAttribute attribute,
			DataAttribute ivoParentDataAttribute) {
		DataAttribute sdAttribute=null;
		if(ivoParentDataAttribute!=null){
			DataLevel normalizedLevel = DataLevel.getNormalizedDataLevelForIvoLevel(ivoParentDataAttribute.getAttributeLevel());
			DataAttribute sdParentAttribute=DataAttributeFactory.getAttributeByNameAndLevel(ivoParentDataAttribute.getAttributeName(), normalizedLevel);
			sdAttribute=DataAttributeFactory.getAttributeByNameAndLevelAndParent(attribute.getAttributeName(), sdParentAttribute.getAttributeLevel(), sdParentAttribute);
		}
		Serializable sdAttributeValue = sdDataRow.getAttributeValue(sdAttribute);
		return sdAttributeValue instanceof DataRow ? (DataRow)sdAttributeValue : null;
	}

}
