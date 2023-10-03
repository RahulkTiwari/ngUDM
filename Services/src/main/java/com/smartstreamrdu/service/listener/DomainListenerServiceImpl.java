/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainListenerServiceImpl.java
 * Author : SaJadhav
 * Date : 20-Aug-2019
 * 
 */
package com.smartstreamrdu.service.listener;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.service.events.DomainContainerChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.EventListener;

/**
 * @author SaJadhav
 *
 */
@Component
public class DomainListenerServiceImpl implements DomainListenerService {
	
	private List<EventListener<? super Serializable>> listeners;
	
	@Autowired
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void setListeners(List<EventListener> lst) {
		this.listeners = (List) lst;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.listener.DomainListenerService#newDomainContainer(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public void newDomainContainer(DataContainer dataContainer) {
		List<EventListener<? super Serializable>> filters = filterOnEvent(ListenerEvent.NewDomainDataContainer);
		propogateEvent(filters,new Object[]{dataContainer});
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.listener.DomainListenerService#domainConainerMerged(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public void domainConainerMerged(DataContainer dataContainer) {
		List<EventListener<? super Serializable>> filters = filterOnEvent(ListenerEvent.DomainContainerMerged);
		propogateEvent(filters,new Object[]{dataContainer});
	}
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.listener.DomainListenerService#domainUpdated(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public void domainUpdated(DataContainer updatedContainer, DataContainer oldContainer) {
		List<EventListener<? super Serializable>> filters = filterOnEvent(ListenerEvent.DomainContainerUpdate);
		propogateEvent(filters,new Object[]{updatedContainer,oldContainer});
	}

	/**
	 * @param filters
	 * @param inputs
	 */
	private void propogateEvent(List<EventListener<? super Serializable>> filters, final Object[] inputs) {
		if(filters.isEmpty()){
			return;
		}
		DomainContainerChangeEventListenerInputCreationContext eventInputContext=new DomainContainerChangeEventListenerInputCreationContext();
		eventInputContext.setDomainContainer((DataContainer) inputs[0]);
		if(inputs.length>1){
			eventInputContext.setOldContainer((DataContainer) inputs[1]);	
		}
		filters.forEach(listener -> 
			listener.propogateEvent(listener.createInput(eventInputContext))
		);
	}

	
	
	private List<EventListener<? super Serializable>> filterOnEvent(ListenerEvent event){
		if (CollectionUtils.isEmpty(listeners)) {
			return Collections.emptyList();
		}
		return listeners.stream().filter(sc -> sc.isEventApplicable(event)).collect(Collectors.toList());
	}
}
