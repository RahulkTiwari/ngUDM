/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: VfsOpenFigiChangeEventListener.java
 * Author: Rushikesh Dedhia
 * Date: Jul 5, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.openfigi.VfsOpenFigiRequestService;
import com.smartstreamrdu.service.openfigi.VfsOpenFigiRequestServiceFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Dedhia
 * NOTE:This class provides services which is specific to sdData only.
 */
@Component
@Slf4j
public class VfsOpenFigiChangeEventListener implements EventListener<ChangeEventInputPojo> {

	@Autowired
	private VfsOpenFigiRequestServiceFactory openFigiRequestServiceFactory;
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#propogateEvent(java.io.Serializable)
	 */
	@Override
	public void propogateEvent(ChangeEventInputPojo changeEventPojo) {
		if (null == changeEventPojo || changeEventPojo.getPostChangeContainer() == null) {
			return;
		}
		DataContainer postChangeContainer = changeEventPojo.getPostChangeContainer();

		VfsOpenFigiRequestService vfsOpenFigiRequestService = openFigiRequestServiceFactory
				.getVfsOpenFigiRequestService(postChangeContainer.getLevel());
		
		if(vfsOpenFigiRequestService!=null) {
			vfsOpenFigiRequestService.sendRequestToVfsOpenFigi(postChangeContainer);
		}
		
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#isEventApplicable(com.smartstreamrdu.service.listener.ListenerEvent)
	 */
	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		
		boolean isEventApplicable = false;
		
		if (ListenerEvent.DataUpdate == event) {
			isEventApplicable = true;
		}
		return isEventApplicable;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#createInput(java.lang.Object[])
	 */
	@Override
	public ChangeEventInputPojo createInput(ChangeEventListenerInputCreationContext inputCreationContext) {

		if (!(inputCreationContext instanceof UpdateChangeEventListenerInputCreationContext)) {
			log.error(
					"{} requires a input creation context of type UpdateChangeEventListenerInputCreationContext. Supplied input creation context was of type {}",
					this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException(
					"Argument of incorrect type supplied input creation is not supported for "
							+ this.getClass().getName());
		}

		UpdateChangeEventListenerInputCreationContext inputContext = (UpdateChangeEventListenerInputCreationContext) inputCreationContext;

		ChangeEventInputPojo inp = new ChangeEventInputPojo();
		inp.setPreChangeContainer(inputContext.getFeedDataContaier());
		inp.setPostChangeContainer(inputContext.getDbDataContainer());
		return inp;
	}

}
