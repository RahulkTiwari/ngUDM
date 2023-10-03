/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: UpdateDomainContainer.java
 * Author : SaJadhav
 * Date : 20-Aug-2019
 * 
 */
package com.smartstreamrdu.service.events;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.util.Constant.ListenerConstants;

/**
 * adds the updDate and updUser attribute values in Domain dataContainer
 * @author SaJadhav
 *
 */
@Component
public class UpdateDomainContainer implements EventListener<DataContainer> {
	
	private static final Logger _logger = LoggerFactory.getLogger(UpdateDomainContainer.class);

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#propogateEvent(java.io.Serializable)
	 */
	@Override
	public void propogateEvent(DataContainer dataContainer) {
		if(!dataContainer.isNew() && dataContainer.hasContainerChanged()){
			setUpdUserAndDate(dataContainer);
		}
	}

	/**
	 * @param dataContainer
	 */
	private void setUpdUserAndDate(DataContainer dataContainer) {
		DataLevel dataLevel = dataContainer.getLevel();
		DataAttribute updDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateDate, dataLevel);
		DataAttribute updUserAttribute = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateUser, dataLevel);
		DataContainerContext context = dataContainer.getDataContainerContext();
		
		DataValue<LocalDateTime> updDateVal=new DataValue<>();
		updDateVal.setValue(LockLevel.RDU, context.getUpdateDateTime());
		dataContainer.addAttributeValue(updDateAttribute, updDateVal);
		
		DataValue<String> updUserVal=new DataValue<>();
		updUserVal.setValue(LockLevel.RDU, context.getUpdatedBy());
		dataContainer.addAttributeValue(updUserAttribute, updUserVal);
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#isEventApplicable(com.smartstreamrdu.service.listener.ListenerEvent)
	 */
	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return event.equals(ListenerEvent.DomainContainerMerged);
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#createInput(com.smartstreamrdu.service.events.ChangeEventListenerInputCreationContext)
	 */
	@Override
	public DataContainer createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		if (!(inputCreationContext instanceof DomainContainerChangeEventListenerInputCreationContext)) {
			_logger.error("{} requires a input creation context of type DomainContainerChangeEventListenerInputCreationContext. Supplied input creation context was of type {}", this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException("Argument of incorrect type supplied input creation is not supported for "+this.getClass().getName());
		}
		DomainContainerChangeEventListenerInputCreationContext context=(DomainContainerChangeEventListenerInputCreationContext) inputCreationContext;
		return context.getDomainContainer();
	}
}
