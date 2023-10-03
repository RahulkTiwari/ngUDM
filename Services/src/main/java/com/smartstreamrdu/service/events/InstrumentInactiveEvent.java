/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InstrumentInactiveEvent.java
 * Author:	Jay Sangoi
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.events.InactiveBean;
import com.smartstreamrdu.service.inactive.InactiveService;
import com.smartstreamrdu.service.inactive.InactiveServiceFactory;
import com.smartstreamrdu.service.listener.ListenerEvent;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class InstrumentInactiveEvent implements EventListener<InactiveBean>{

	@Autowired
	private InactiveServiceFactory factory;
	
	private static final Logger _logger = LoggerFactory.getLogger(InstrumentInactiveEvent.class);

	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#propogateEvent(java.io.Serializable)
	 */
	@Override
	public void propogateEvent(InactiveBean bean) {
		InactiveService inactiveService = factory.getInactiveService(bean);
		if(inactiveService != null){
			try {
				inactiveService.inactivateIfRequired(bean);
			} catch (Exception e) {
				_logger.error("Exception in InstrumentInactiveEvent",e);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#isEventApplicable(com.smartstreamrdu.service.listener.ListenerEvent)
	 */
	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return ListenerEvent.DataContainerMerging  == event;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#createInput(java.lang.Object[])
	 */
	@Override
	public InactiveBean createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		
		if (!(inputCreationContext instanceof DataContainerMergingChangeEventListenerInputCreationContext)) {
			_logger.error("{} requires a input creation context of type InstrumentInactiveChangeEventListenerInputCreationContext. Supplied input creation context was of type {}", this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException("Argument of incorrect type supplied input creation is not supported for "+this.getClass().getName());
		}
		
		DataContainerMergingChangeEventListenerInputCreationContext inputContext = (DataContainerMergingChangeEventListenerInputCreationContext) inputCreationContext;
		
		InactiveBean bean = new InactiveBean();
		bean.setDbContainer(inputContext.getDataContainer());
		bean.setDatasource(inputContext.getDataSource());
		return bean;
	}
	

}
