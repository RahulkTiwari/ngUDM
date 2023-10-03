/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ListnerServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.listener;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.service.events.DataContainerMergingChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.EventListener;
import com.smartstreamrdu.service.events.MergeCompleteChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.NewDataContainerChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.UpdateChangeEventListenerInputCreationContext;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class ListenerServiceImpl implements ListenerService{

	private List<EventListener<? super Serializable>> listeners;
	
	@Autowired
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void setListeners(List<EventListener> lst) {
		this.listeners = (List) lst;
	}
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.listener.ListenerService#dataContainerUpdated(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public void dataContainerUpdated(String datasource, DataContainer oldContainer,
			DataContainer newContainer,FeedConfiguration feedConfiguration) {

		if (CollectionUtils.isEmpty(listeners)) {
			return;
		}

		List<EventListener<? super Serializable>> filters = filterOnEvent(ListenerEvent.DataUpdate);
		
		if(filters.isEmpty()){
			return;
		}
		
		filters.forEach(listener -> {
			UpdateChangeEventListenerInputCreationContext inputCreationContext = new UpdateChangeEventListenerInputCreationContext();
			inputCreationContext.setDbDataContainer(newContainer);
			inputCreationContext.setFeedDataContaier(oldContainer);
			inputCreationContext.setFeedConfiguration(feedConfiguration);
			listener.propogateEvent(listener.createInput(inputCreationContext));
		});
		
	}


	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.listener.ListenerService#dataContainerMerge(com.smartstreamrdu.domain.FeedConfiguration, com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public void dataContainerMerge(String datasource, DataContainer feedContainer, DataContainer dbContainer) {

		if (CollectionUtils.isEmpty(listeners)) {
			return;
		}

		List<EventListener<? super Serializable>> filters = filterOnEvent(ListenerEvent.DataContainerMerging);
		
		if(filters.isEmpty()){
			return;
		}
		
		filters.forEach(listener -> {
			DataContainerMergingChangeEventListenerInputCreationContext inputCreationContext = new DataContainerMergingChangeEventListenerInputCreationContext();
			inputCreationContext.setDataContainer(dbContainer);
			inputCreationContext.setDataSource(datasource);
			listener.propogateEvent(listener.createInput(inputCreationContext));
		}
		);
	}


	@Override
	public void newdataContainer(DataContainer container) {

		if (CollectionUtils.isEmpty(listeners)) {
			return;
		}
		List<EventListener<? super Serializable>> filters = filterOnEvent(ListenerEvent.NewDataContainer);
		
		if(filters.isEmpty()){
			return;
		}
		
		filters.forEach(listener -> {
			NewDataContainerChangeEventListenerInputCreationContext inputCreationContext = new NewDataContainerChangeEventListenerInputCreationContext();
			inputCreationContext.setDataContainer(container);
			listener.propogateEvent(listener.createInput(inputCreationContext));
		});

	}


	@Override

	public void mergeComplete(String datasource, DataContainer feedContainer, DataContainer dbContainer) {
		
		if(CollectionUtils.isEmpty(listeners)){
			return;
		}
		
		List<EventListener<? super Serializable>> filters =  filterOnEvent(ListenerEvent.MergeComplete);
		
		filters.forEach(listener ->{
			MergeCompleteChangeEventListenerInputCreationContext inputContext = new MergeCompleteChangeEventListenerInputCreationContext();
			inputContext.setDataContainer(dbContainer);
			inputContext.setDataSource(datasource);
			listener.propogateEvent(listener.createInput(inputContext));
		});
		
	}
	
	private List<EventListener<? super Serializable>> filterOnEvent(ListenerEvent event){
		return listeners.stream().filter(sc -> sc.isEventApplicable(event)).collect(Collectors.toList());
		
	}
}
