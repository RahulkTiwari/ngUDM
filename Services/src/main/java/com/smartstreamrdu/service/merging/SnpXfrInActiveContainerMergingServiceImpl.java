/*******************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility
 * All rights reserved.
 *
 * File:    SnpXfrInActiveContainerMergingStrategy.java
 * Author:    Padgaonkar
 * Date:    05-Mar-2020
 *
 *******************************************************************
 **/
package com.smartstreamrdu.service.merging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.util.Constant.SdAttributeNames;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Padgaonkar
 *
 *for snp we want to extract instrumentStatus from feedContainer and update only this attribute in dbContaier and persist dbContainer.
 */
@Component("snpXfrStatusIMerger")
@Slf4j
public class SnpXfrInActiveContainerMergingServiceImpl implements DataContainerMergingService {

	@Autowired
	private ContainerComparatorFactory containerComparatorFactory;

	@Autowired
	private transient SimpleAttributeMergeHandler simpleAttributeMergeHandler;

	/**
	 * 
	 */
	private static final long serialVersionUID = 3516075435036427372L;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DataContainer> merge(DataContainer feedContainer, List<DataContainer> dbContainers)
			throws DataContainerMergeException {
		List<DataContainer> dataContainerList = new ArrayList<>();

		ParentContainerComparator parentContainerComparator = containerComparatorFactory.getParentContainerComparator(feedContainer);

		DataContainer dbContainer=null;
		
			try {
				//find exact applicable dbcontainer from list of dbContainer for this feedcontainer
				dbContainer = parentContainerComparator.compareDataContainer(feedContainer, dbContainers);

			} catch (UdmTechnicalException e) {
				
				throw new DataContainerMergeException("error while getting parent container comparator: "+e);
			}
		
		if (dbContainer == null) {
			log.error("dbCotainer not found for snp inactivation event: feed container: {}, dbContainer: {}", feedContainer, dbContainer);
			dataContainerList.add(feedContainer);
			return dataContainerList;
		}
		
		dbContainer.updateDataContainerContext(feedContainer.getDataContainerContext());
        //check status
		simpleAttributeMergeHandler.handleAttributeMerge(feedContainer, dbContainer,
				DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_STATUS, DataLevel.INS));
		
		dataContainerList.add(dbContainer);

		return dataContainerList;
	}

	
	@Override
	public DataContainer getFeedEquivalentParentDbContainer(DataContainer feedContainer,
			List<DataContainer> dbContainers) throws UdmTechnicalException {
		//this method is basically called for security level feed.
		throw new UnsupportedOperationException();
	}

	@Override
	public DataContainer getFeedEquivalentChildDbContainer(DataContainer feedParentContainer,
			DataContainer feedChildContainer, List<DataContainer> dbContainers) throws UdmTechnicalException {
		//this method is basically called for security level feed.
		throw new UnsupportedOperationException();
	}

}
