/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	AttributeMergingDecorator.java
 * Author:	Jay Sangoi
 * Date:	24-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.util.LambdaExceptionUtil;

/**
 * This does merging of Data Container at attribute level
 * 
 * @author Jay Sangoi
 *
 */
@Component
@Scope("prototype")
public class AttributeMergingDecorator implements DataContainerMerging {

	@Autowired
	private ContainerComparatorFactoryImpl containerComparatorFactory;

	@Autowired
	private AttributeMergeHandlerFactoryImpl mergeHandlerFactory;

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

		if (feedContainer == null) {
			throw new DataContainerMergeException("Feed container cannot be null");
		}

		if (dbContainers == null) {
			dbContainers = new ArrayList<>();
		}

		ParentContainerComparator parentContainerComparator = containerComparatorFactory
				.getParentContainerComparator(feedContainer);

		DataContainer equivalentContainerFromDb = parentContainerComparator.compareDataContainer(feedContainer,
				dbContainers);

		if (equivalentContainerFromDb == null) {
			dbContainers.add(feedContainer);
			return;
		}
		
		equivalentContainerFromDb.updateDataContainerContext(feedContainer.getDataContainerContext());
		
		mergeAttribute(feedContainer, equivalentContainerFromDb);
		mergeChildContainerAttribute(feedContainer, equivalentContainerFromDb);

	}

	private void mergeChildContainerAttribute(DataContainer feedContainer, DataContainer childContainer) throws Exception {
		List<DataContainer> feedChildCotainers = feedContainer.getAllChildDataContainers();


		if (CollectionUtils.isEmpty(feedChildCotainers)) {
			return;
		}

		List<DataContainer> dbChildContainers = childContainer.getAllChildDataContainers();

		final List<DataContainer> dbCldContainers = dbChildContainers == null ? new ArrayList<>() : dbChildContainers;

		
		ChildContainerComparator childContainerComparator = containerComparatorFactory
				.getChildContainerComparator(feedContainer);

		feedChildCotainers.forEach(LambdaExceptionUtil.rethrowConsumer(feedCC -> {
			DataContainer compare = childContainerComparator.compare(feedContainer, feedCC, dbCldContainers);
			if (compare != null) {
				compare.updateDataContainerContext(feedCC.getDataContainerContext());
				mergeAttribute(feedCC, compare);
			}
		}));

	}

	/**
	 * @param feedContainer
	 * @param equivalentContainerFromDb
	 */
	private void mergeAttribute(DataContainer feedContainer, DataContainer equivalentContainerFromDb) {
		Map<DataAttribute, DataValue<Serializable>> feedRecordData = feedContainer.getRecordData();

		Set<DataAttribute> dataAttributes = feedRecordData.keySet();

		for (DataAttribute dataAttribute : dataAttributes) {

			AttributeMergeHandler mergeHandler = mergeHandlerFactory.getMergeHandler(dataAttribute);

			mergeHandler.handleAttributeMerge(feedContainer, equivalentContainerFromDb, dataAttribute);

		}
	}
}
