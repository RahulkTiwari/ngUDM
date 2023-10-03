/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: NewDomainContainerEventListener.java
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
 * Whenever new domain map or new domain is added then this listener is called to populate the 
 * insUser and insDate attributes
 * @author SaJadhav
 *
 */
@Component
public class NewDomainContainerEventListener implements EventListener<DataContainer> {
	
	private static final Logger _logger = LoggerFactory.getLogger(NewDomainContainerEventListener.class);
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#propogateEvent(java.io.Serializable)
	 */
	@Override
	public void propogateEvent(DataContainer dataContainer) {
		DataLevel dataLevel = dataContainer.getLevel();
		DataAttribute insDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertDate, dataLevel);
		DataAttribute insUserAttribute = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertUser, dataLevel);
		DataContainerContext context = dataContainer.getDataContainerContext();
		
		DataValue<LocalDateTime> insDateVal=new DataValue<>();
		insDateVal.setValue(LockLevel.RDU, context.getUpdateDateTime());
		dataContainer.addAttributeValue(insDateAttribute, insDateVal);
		
		DataValue<String> insUserVal=new DataValue<>();
		insUserVal.setValue(LockLevel.RDU, context.getUpdatedBy());
		dataContainer.addAttributeValue(insUserAttribute, insUserVal);
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#isEventApplicable(com.smartstreamrdu.service.listener.ListenerEvent)
	 */
	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return event.equals(ListenerEvent.NewDomainDataContainer);
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
