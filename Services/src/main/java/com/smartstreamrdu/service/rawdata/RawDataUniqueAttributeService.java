/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataUniqueAttributeService.java
 * Author:	Padgaonkar S
 * Date:	29-Aug-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;

import javax.annotation.concurrent.ThreadSafe;

import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.rules.RduRule;
/**
 * This service will take input  as ruleScript.Executes rule on given record object 
 * and return ruleOutput.
 */
@ThreadSafe
public interface RawDataUniqueAttributeService {
	
	public Serializable getRawDataUniqueAttributeValue(String dataSource,Record record,RduRule rule);

}
