/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: SimpleAttributeMergeHandler.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.AuditField;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.HistorizedData;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.UdmErrorCodes;
import com.smartstreamrdu.service.ivo.LockRemover;
import com.smartstreamrdu.util.DataContainerUtil;

/**
 * @author Dedhia
 *
 */
@Component
public class SimpleAttributeMergeHandler implements AttributeMergeHandler {

	private static final Logger _logger = LoggerFactory.getLogger(SimpleAttributeMergeHandler.class);

	private static final Marker AUDIT_MARKER = MarkerFactory.getMarker("Audit");
	
	@Autowired
	private List<LockRemover> lockRemovers;
	
	
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.AttributeMergeHandler#handleAttributemerge(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handleAttributeMerge(DataContainer feedContainer, DataContainer dbContainer,
			DataAttribute dataAttribute) {

		DataValue<Serializable> feedValue = (DataValue<Serializable>) feedContainer.getAttributeValue(dataAttribute);
		DataValue<Serializable> dbValue = (DataValue<Serializable>) dbContainer.getAttributeValue(dataAttribute);

		if (dbValue == null) {
			dbContainer.addAttributeValue(dataAttribute, feedValue);
			return;
		}

		Set<LockLevel> locks = feedValue.getLockData().keySet();
		locks.forEach(l -> {
			HistorizedData<Serializable> lockValue = feedValue.getLockValue(l);
			Optional<UdmErrorCodes> fErrorCode = feedValue.getErrorCode(l);
			Serializable dVal = dbValue.getValue(l);
			
			if(_logger.isDebugEnabled(AUDIT_MARKER)) {
				auditSafely(dbContainer, dataAttribute, l, lockValue.getValue(), dVal, fErrorCode.orElse(null));
			}
			
			dbValue.setValue(l, lockValue.getValue(), lockValue.getErrorCode(), lockValue.getLockLevelInfo());
		});
		String dataSource = feedContainer.getDataContainerContext().getDataSource();
		lockRemovers.forEach(lockRemover -> lockRemover.removeLockValue(dataAttribute,feedValue,dbValue,new DomainType(dataSource)));
		// addAttributeValue is impacting the hasChanged value of attributes. As part of
		// addAttributeValue it will call merge() which will update valueMap with
		// newValue and which sets hasChaged value to true, if newValue is different
		// from oldValue in valueMap. As we already update valueMap by
		// dbValue.setValue(). addAttributeValue again check the same with updated data
		// that cause hasChanged to false , hence even if value is changed it will
		// return false
		if (dbValue.hasValueChanged() && DataContainerUtil.isAuditableAttribute(dataAttribute)) {
			dbContainer.setHasChanged(true);
		}
	}

	/**
	 * @param dbContainer
	 * @param dataAttribute
	 * @param l
	 * @param fVal
	 * @param dVal
	 * @param udmErrorCode 
	 */
	protected void auditSafely(DataContainer dbContainer, DataAttribute dataAttribute, LockLevel l, Serializable fVal,
			Serializable dVal, UdmErrorCodes udmErrorCode) {
		try {
			if (!Objects.equals(fVal, dVal)) {
				String attributeName = dataAttribute.getAttributeName();
				AuditField field = new AuditField(attributeName, l);
				field.setOldValue((udmErrorCode != null ? udmErrorCode.getErrorCode() : dVal));
				field.setNewValue(fVal);
				dbContainer.addAuditField(field);
			}
		} catch (Exception e) {
			_logger.warn("Error while writing audit data to container", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.AttributeMergeHandler#handleAttributeMerge(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataAttribute, java.util.Map, java.util.Map)
	 */
	@Override
	public void handleAttributeMerge(DataRow feedDataRow, DataRow dbDataRow, DataAttribute dataAttribute, DomainType dataSource) {
		
		DataValue<Serializable> feedValue = feedDataRow.getAttributeValue(dataAttribute);
		DataValue<Serializable> dbValue = dbDataRow.getAttributeValue(dataAttribute);
		
		if (dbValue == null) {
			dbDataRow.addAttribute(dataAttribute, feedValue);
			return;
		}
		
		feedValue.getLockData().keySet().forEach(sc-> {
			HistorizedData<Serializable> lockValue = feedValue.getLockValue(sc);
			dbValue.setValue(sc, lockValue.getValue(), lockValue.getErrorCode(), lockValue.getLockLevelInfo());	
		});
		lockRemovers.forEach(lockRemover -> lockRemover.removeLockValue(dataAttribute,feedValue,dbValue,dataSource));
	}

}
