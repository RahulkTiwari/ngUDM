/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ProformaMessageGenerationHandlerFactory.java
 * Author: Rushikesh Dedhia
 * Date: October 04, 2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.xrf.messaging;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.util.DataSourceConstants;

@Component
public class ProformaMessageGenerationHandlerFactory {
	
	public ProformaMessageGenerationHandler getProformaMessageGenerationHandler(String dataSource) {
		
		Objects.requireNonNull(dataSource);
		
		if (DataSourceConstants.GLEIF_DS.equals(dataSource)) {
			return SpringUtil.getBean(GleifProformaMessageGenerationHandler.class);
		} else {
			return SpringUtil.getBean(DefaultProformaMessageGenerationHandler.class);
		} 
		
	}

}
