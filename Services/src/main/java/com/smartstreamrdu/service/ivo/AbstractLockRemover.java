/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: AbstractLockRemover.java
 * Author:Shreepad Padgaonkar
 * Date: Nov 20, 2020
 *
 *******************************************************************/
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public abstract class AbstractLockRemover implements LockRemover {

	@Setter
	@Autowired
	private NormalizedValueService normalizedValueService;
	
	/**
	 * This method is applicable for domainable attributes only.
	 * If normalized value for feed value matches with NormalizedLock value then this method
	 * removes lock value.
	 * 
	 * @param dataAttribute
	 * @param feedValue
	 * @param mergedDbValue
	 * @param lockLevel
	 * @param dataSource
	 */
	protected void checkAndRemoveLockForDomainAttribute(DataAttribute dataAttribute,
			DataValue<Serializable> mergedDbValue, LockLevel lockLevel,DomainType dataSource) {

		//If dataAttribute is not of domainType do nothing.
		if(!dataAttribute.getDataType().equals(DataType.DOMAIN)) {
			return;
		}
		 
		DomainType dbValue = (DomainType) mergedDbValue.getValue(lockLevel);
		DomainType feedVal = (DomainType) mergedDbValue.getValue(LockLevel.FEED);
		if(Objects.isNull(dbValue) || Objects.isNull(feedVal)) {
			return;
		}

		String dbNormalizedValue = dbValue.getNormalizedValue();
		
		if (dataSource == null) {
			log.debug(
					"For requested domain attribute:{} lockRemoval of normalized domain value :{} is not posssible as dataSource is null",
					dataAttribute, mergedDbValue);
			return;
		}
		
		Serializable normalizedValueForFeedValue=feedVal.getNormalizedValue();
		if (normalizedValueForFeedValue == null) {
			normalizedValueForFeedValue = normalizedValueService.getNormalizedValueForDomainValue(dataAttribute,
					feedVal, dataSource.getVal());
		}
		//If normalized value for feed value is non-null && it equals to requested lockvalue
		//Then remove lock.
		if(!Objects.isNull(normalizedValueForFeedValue) && normalizedValueForFeedValue.equals(dbNormalizedValue)) {
			mergedDbValue.removeLockValue(lockLevel);
			log.debug("Requested Lock : {}  removed for attribute:{},value:{}",lockLevel,dataAttribute,normalizedValueForFeedValue);
		}
		
	}
}
