/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: EnrichmentLockRemover.java
 * Author:Shreepad Padgaonkar
 * Date: Nov 20, 2020
 *
 *******************************************************************/
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;

import lombok.extern.slf4j.Slf4j;

/**
 * This Lock Remover class is used to remove enrichmentLock value based on conditions.
 * @author Padgaonkar
 *
 */
@Component
@Slf4j
public class EnrichmentLockRemover  extends AbstractLockRemover implements LockRemover {

	@Override
    public void removeLockValue(DataAttribute dataAttribute, DataValue<Serializable> feedValue,
			DataValue<Serializable> mergedDbValue, DomainType dataSource) {
		
		Serializable dbEnrichmentValue = mergedDbValue.getValue(LockLevel.ENRICHED);
		
		//If no value at enrichment Lock present in merged dbValue do nothing 
		if(Objects.isNull(dbEnrichmentValue)) {
			return;
		}
		
		Serializable feedEnrichmentValue = feedValue.getValue(LockLevel.ENRICHED);
		Serializable dbFeedValue = mergedDbValue.getValue(LockLevel.FEED);

		//If ruleOutput(FeedValue) doesn't contain enrichment Lock then remove Lock from dbValue
		if(Objects.isNull(feedEnrichmentValue) || dbEnrichmentValue.equals(dbFeedValue)) {
			mergedDbValue.removeLockValue(LockLevel.ENRICHED);
			log.debug("Enrichemnt Lock removed for attribute:{},value:{}",dataAttribute,feedEnrichmentValue);
		}
		
       checkAndRemoveLockForDomainAttribute(dataAttribute,mergedDbValue,LockLevel.ENRICHED,dataSource);
	}

}
