/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: AttributeMergeHandlerFactoryImpl.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataType;

/**
 * @author Dedhia
 *
 */
@Component
public class AttributeMergeHandlerFactoryImpl implements AttributeMergeHandlerFactory {
	
	@Autowired
	private SimpleAttributeMergeHandler simpleAttributeMergeHandler;
	
	@Autowired
	private NestedAttributeMergeHandler nestedAttributeMeregeHandler;
	
	@Autowired
	private NestedArrayAttributeMergeHandler nestedArrayAttributeMergeHandler;
	

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.AttributeMergeHandlerFactory#getMergeHandler(com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public AttributeMergeHandler getMergeHandler(DataAttribute dataAttribute) {
		
		if (dataAttribute == null) {
			throw new IllegalArgumentException("The dataAttribute cannot be null.");
		}
		
		DataType dataType = dataAttribute.getDataType();
		
		if (DataType.NESTED.equals(dataType)) {
			return nestedAttributeMeregeHandler;
		} else if (DataType.NESTED_ARRAY.equals(dataType)) {
			return nestedArrayAttributeMergeHandler;
		}	 else {
			return simpleAttributeMergeHandler;
		}
		
	}
}
