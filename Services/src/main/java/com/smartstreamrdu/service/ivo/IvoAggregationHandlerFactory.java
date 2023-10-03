/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoAggregationHandlerFactory.java
 * Author : SaJadhav
 * Date : 13-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataType;

/**
 * @author SaJadhav
 *
 */
@Component
public class IvoAggregationHandlerFactory {
	@Autowired
	private NestedArrayAttributeIvoAggregationHandler nestedArrayAttributeIvoAggregationHandler;
	@Autowired
	private SimpleAttributeIvoMergeHandler simpleAttributeIvoAggregationHandler;
	@Autowired
	private NestedAttributeIvoAggregationHandler nestedAttributeIvoAggregationHandler;

	public IvoAggregationHandler getIvoAggregationHandler(DataAttribute dataAttribute){
		Objects.requireNonNull(dataAttribute, "dataAttribute should be populated");
		DataType dataType = dataAttribute.getDataType();
		
		if (DataType.NESTED.equals(dataType)) {
			return nestedAttributeIvoAggregationHandler;
		} else if (DataType.NESTED_ARRAY.equals(dataType)) {
			return nestedArrayAttributeIvoAggregationHandler;
		} else {
			return simpleAttributeIvoAggregationHandler;
		}
	}
}
