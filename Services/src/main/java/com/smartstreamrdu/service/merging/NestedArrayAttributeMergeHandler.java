/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: NestedArrayAttributeMergeHandler.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;

/**
 * @author Dedhia
 *
 */
@Component
public class NestedArrayAttributeMergeHandler implements AttributeMergeHandler {
	
	
	@Autowired
	private NestedArrayAttributeMergingServiceFactory nestedArrayAttributeMergingServiceFactory;
	
	@Autowired
	private SDNestedArrayComparator sdNestedArrayComparator;
	
	@Autowired
	private NestedAttributeMergeHandler nestedHandler;
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.AttributeMergeHandler#handleAttributemerge(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public void handleAttributeMerge(DataContainer feedContainer, DataContainer dbContainer, DataAttribute dataAttribute) {
		
		// Get the appropriate merge service.
		NestedArrayAttributeMergingService mergingService = nestedArrayAttributeMergingServiceFactory.getMergingService(feedContainer);
		
		mergingService.merge(feedContainer, dbContainer, dataAttribute);
		
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.smartstreamrdu.service.merging.AttributeMergeHandler#handleAttributeMerge
		 * (com.smartstreamrdu.domain.DataContainer,
		 * com.smartstreamrdu.domain.DataAttribute, java.util.Map, java.util.Map)
		 */
		@Override
		public void handleAttributeMerge(DataRow feedDataRow, DataRow dbDataRow, DataAttribute attribute,
				DomainType dataSource) {
			DataRowIterator feedIterator = new DataRowIterator(feedDataRow, attribute);

			while (feedIterator.hasNext()) {
				DataRow feedRow = feedIterator.next();
				DataRow dbRow = getFeedMatchingDbRow(feedRow, dbDataRow, attribute, dataSource);
				if (dbRow == null) {
					dbRow = handleNoDbRow(dbDataRow, attribute, feedRow);
				}
				nestedHandler.handleAttributeMerge(feedRow, dbRow, attribute, dataSource);
			}

		}

		/**
		 * 
		 * Since there is no entry for the given feedRow in the dbRow, we will need to
		 * create a new entry in the dbRow to ensure that the data is not lost.
		 * 
		 * @param dbDataRow
		 * @param attribute
		 * @param feedRow
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private DataRow handleNoDbRow(DataRow dbDataRow, DataAttribute attribute, DataRow feedRow) {
			DataValue<ArrayList<DataRow>> value = (DataValue<ArrayList<DataRow>>) dbDataRow.getAttributeValue(attribute)
					.getValue();
			value.getValue().add(feedRow);
			return feedRow;
		}

		/**
		 * This method will finding and return dbDataRow corresponding to feedDataRow
		 * 
		 * @param feedDataRow
		 * @param dbDataRow
		 * @param attribute
		 * @param dataSource
		 * @return
		 */
		private DataRow getFeedMatchingDbRow(DataRow feedDataRow, DataRow dbDataRow, DataAttribute attribute,
				DomainType dataSource) {
			DataRow row = null;
			DataRowIterator dbIterator = new DataRowIterator(dbDataRow, attribute);
			while (dbIterator.hasNext()) {
				DataRow dbRow = dbIterator.next();
				if (sdNestedArrayComparator.allKeysEquate(feedDataRow, dbRow, attribute, dataSource)) {
					row = dbRow;
					break;
				}
			}
			return row;
		}

}
