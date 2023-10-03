/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerMergingServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	05-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author Jay Sangoi
 *
 */
@Component("DefaultMerger")
@Primary
public class DataContainerMergingServiceImpl implements DataContainerMergingService{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8166437004684156713L;
	
	private static final Logger _logger = LoggerFactory.getLogger(DataContainerMergingServiceImpl.class);
	
	
	@Autowired
	private DataContainerMergingDecoratorBuilder builder;
	
	@Autowired
	private ContainerComparatorFactoryImpl containerComparatorFactory;

	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.DataContainerMergingService#merge(com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public List<DataContainer> merge(DataContainer feedContainer, List<DataContainer> dbContainers)
			throws DataContainerMergeException {
		try {
			builder.createDataContainerMergingDecorator(feedContainer, dbContainers).merge(feedContainer, dbContainers);
		} catch (Exception e) {
			_logger.error("Following error occured while merging datacontainers{}", e);
			throw new DataContainerMergeException("Exception in merging"
					+ "", e);
		}
		return dbContainers;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.DataContainerMergingService#getDbContainer(com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public DataContainer getFeedEquivalentParentDbContainer(DataContainer feedContainer, List<DataContainer> dbContainers) throws UdmTechnicalException {
		
		if (dbContainers == null) {
			dbContainers = new ArrayList<>();
		}

		ParentContainerComparator parentContainerComparator = containerComparatorFactory
				.getParentContainerComparator(feedContainer);

		return parentContainerComparator.compareDataContainer(feedContainer,
				dbContainers);
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.DataContainerMergingService#getFeedEquivalentChildDbContainer(com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public DataContainer getFeedEquivalentChildDbContainer(DataContainer feedParentContainer, DataContainer feedChildContainer,
			List<DataContainer> dbContainers) throws UdmTechnicalException  {
		if (dbContainers == null) {
			dbContainers = new ArrayList<>();
		}

		ChildContainerComparator childComparator = containerComparatorFactory
				.getChildContainerComparator(feedParentContainer);

		try {
			return childComparator.compare(feedParentContainer,feedChildContainer,
					dbContainers);
		} catch (Exception e) {
			throw new UdmTechnicalException("Exception in getFeedEquivalentChildDbContainer", e);
		}
	}
	
	

}
