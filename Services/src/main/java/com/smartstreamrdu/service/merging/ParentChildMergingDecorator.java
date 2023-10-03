/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ParentChildMergingDecorator.java
 * Author:	Jay Sangoi
 * Date:	24-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.util.LambdaExceptionUtil;

/**
 * @author Jay Sangoi
 *
 */
@Component
@Scope("prototype")
public class ParentChildMergingDecorator extends DataContainerMergingDecorator {

	@Autowired
	private ContainerComparatorFactory containerComparatorFactory;

	/**
	 * @param decorator
	 */
	public ParentChildMergingDecorator(DataContainerMerging decorator) {
		super(decorator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.merging.DataContainerMergingDecorator#merge(
	 * com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public void merge(DataContainer feedContainer, List<DataContainer> dbContainers)
			throws Exception {
		ParentContainerComparator parentContainerComparator = containerComparatorFactory
				.getParentContainerComparator(feedContainer);

		DataContainer dbContainer = parentContainerComparator.compareDataContainer(feedContainer, dbContainers);

		if (dbContainer == null) {
			return;
		}

		List<DataContainer> feedChildCotainers = feedContainer.getAllChildDataContainers();

		if (CollectionUtils.isEmpty(feedChildCotainers)) {
			return;
		}
		
		List<DataContainer> dbChildContainers = dbContainer.getAllChildDataContainers();
		
		final List<DataContainer> dbCldContainers = dbChildContainers == null ? new ArrayList<>() : dbChildContainers;
		
		DataAttribute datasourceAttribute = DataAttributeFactory.getDatasourceAttribute(dbContainer.getLevel());
		AtomicReference<String> datasource = new AtomicReference<>();
		Serializable dsVal = feedContainer.getAttributeValueAtLevel(LockLevel.FEED, datasourceAttribute);

		if (dsVal instanceof DomainType) {
			datasource.set(((DomainType) dsVal).getVal());
		}

		ChildContainerComparator childContainerComparator = containerComparatorFactory
				.getChildContainerComparator(feedContainer);

		feedChildCotainers.forEach(LambdaExceptionUtil.rethrowConsumer(feedCC -> {
			DataContainer compare = childContainerComparator.compare(feedContainer,feedCC, dbCldContainers);
			if (compare == null && childContainerComparator.shouldBeAdded(feedCC, datasource.get())) {
					dbContainer.addDataContainer(feedCC, feedCC.getLevel());
			}
		}));

		super.merge(feedContainer, dbContainers);
	}

}
