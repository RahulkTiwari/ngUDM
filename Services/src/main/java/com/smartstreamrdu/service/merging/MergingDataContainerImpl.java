/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MergingDataContainerImpl.java
 * Author:	Jay Sangoi
 * Date:	13-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.LockLevel;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class MergingDataContainerImpl implements MergingDataContainer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5562476692235987948L;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.MergingDataContainer#merge(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public DataContainer merge(DataContainer feedContainer, DataContainer dbContainer) {
		if(dbContainer == null){
			return feedContainer;
		}
		if(feedContainer == null){
			return dbContainer;
		}
		//Both feed and db container are not null, hence we need to merge it
		
		//Copy db container id to feed container
		feedContainer.set_id(dbContainer.get_id());

		//Merge child container
		megeChildContainer(feedContainer, dbContainer);
		
		return feedContainer;
	}

	/**
	 * @param feedContainer
	 * @param dbContainer
	 */
	private void megeChildContainer(DataContainer feedContainer, DataContainer dbContainer) {
		
		List<DataContainer> dbChildContainer = getChildContainers(dbContainer.getChildDataContainers());
		
		if(CollectionUtils.isNotEmpty(dbChildContainer)){
			
			List<DataContainer> feedChildContainers = getChildContainers(feedContainer.getChildDataContainers());
			
			dbChildContainer.forEach(sc->{
				//Check if db child container is in feed child container, if no then add in
				boolean isPresent = isChildContainerPresentInFeed(feedChildContainers, sc);
				if(isPresent){
					//TODO : WE need to merge db and feed child container
				}
				else{
					//add it to feed dat container
					feedContainer.addDataContainer(sc, sc.getLevel());
				}
			});
			

		}
	}
	
	
	private boolean isChildContainerPresentInFeed(List<DataContainer> feedChildContainers, DataContainer childContainer){
		if(childContainer != null && CollectionUtils.isNotEmpty(feedChildContainers)){
			return feedChildContainers.stream().anyMatch(sc->{
				Serializable dbValue = childContainer.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(childContainer.getLevel()));
				Serializable feedValue = sc.getAttributeValueAtLevel(LockLevel.FEED, DataAttributeFactory.getSourceUniqueIdentifierForLevel(sc.getLevel()));
				return (dbValue == feedValue || (dbValue != null && dbValue.equals(feedValue)));
			});
		}
		return false;
	}

	private List<DataContainer> getChildContainers(Map<DataAttribute, List<DataContainer>> childContainers) {
		List<DataContainer> containers = new ArrayList<>();
		if (childContainers != null) {
			childContainers.values().forEach(containers::addAll);
		}
		return containers;
	}
	
}
