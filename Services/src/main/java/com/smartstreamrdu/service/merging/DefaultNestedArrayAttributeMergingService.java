/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    DefaultNestedArrayAttributeMergingService.java
 * Author:  Dedhia
 * Date:    16-Jul-2020
 *
 ********************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RduLockLevelInfo;

import lombok.Setter;

/**
 *  Default implementation for merging of NestedArrayAttributes.
 *  This merge strategy merges the Feed data with the existing data in the database
 *  data container. Locks for all the nested structures will be retained. 
 *  No nested block will be lost between the feed and the database data containers.
 *  This merge strategy creates a merged super set of all data between the feed and the 
 *  database data container for the nested array attributes. 
 *  
 * @author Dedhia
 *
 */
@Component("DefaultNestedArrayAttributeMergingService")
public class DefaultNestedArrayAttributeMergingService implements NestedArrayAttributeMergingService {
	
	@Autowired
	@Setter
	private NestedAttributeMergeHandler nestedHandler;
	
	@Autowired
	@Setter
	private ContainerComparatorFactory factory;
	
	private static final Logger _logger = LoggerFactory.getLogger(DefaultNestedArrayAttributeMergingService.class);

	public void merge(DataContainer feedContainer, DataContainer dbContainer, DataAttribute dataAttribute) {
		
		DataRowIterator feediterator = new DataRowIterator(feedContainer, dataAttribute);

		NestedArrayComparator nestedArrayComparator = factory.getNestedArrayComparator(feedContainer);
		
		if(nestedArrayComparator == null){
			_logger.warn("No NestedArrayComparator found for {}. Hence not performing NestedArrayAttributeMergeHandler",feedContainer);
			return;
		}
		while(feediterator.hasNext()){
			DataRow row = feediterator.next();
			DataRowIterator dbiterator = new DataRowIterator(dbContainer, dataAttribute);

			NestedArrayComparatorInputPojo inputPojo = NestedArrayComparatorInputPojo.builder().feed(row).dbIterator(dbiterator)
					.feedDataContainer(feedContainer).dbDataContainer(dbContainer).arrayAttribute(dataAttribute).build();

			DataRow dbRow = nestedArrayComparator.compare(inputPojo);

			if(dbRow == null){
				dbRow = handleNoDbRow(dbContainer, dataAttribute, row);
			}
			else{
				//perform nested merging
				String dataSource = feedContainer.getDataContainerContext().getDataSource();
				nestedHandler.handleAttributeMerge(row, dbRow, null,new DomainType(dataSource));
			}
			if(!dbContainer.hasContainerChanged() && dbRow.hasValueChanged()){
				dbContainer.setHasChanged(true);
			}

		}


	}

	/**
	 * @param dbContainer
	 * @param dataAttribute
	 * @param row
	 * @return
	 */
	protected DataRow handleNoDbRow(DataContainer dbContainer, DataAttribute dataAttribute, DataRow row) {
		//Need to add in array
		Serializable attributeValue = dbContainer.getAttributeValue(dataAttribute);
		DataValue<ArrayList<DataRow>> val = attributeValue instanceof DataRow ? ((DataRow)attributeValue).getValue() : new DataValue<>()   ;
		ArrayList<DataRow> value = val.getValue();
		if(value == null ){
			value = new ArrayList<>();
			val.setValue(LockLevel.RDU, value, new RduLockLevelInfo());
		}
		value.add(row);

		// Since there is no entry for the given nested array structure in the database,
		// we will need to create a new entry in the database data container to ensure that the data is not lost.
		// In the earlier in implementation, this was missed and hence in cases where there was a update to  not null
		// for NESTED_ARRAY attributes, it failed to update the container with the new incoming data.
		// Adding this new value to the data container takes care of that issue.
		if (attributeValue == null) {
			addNewDataRowToDbContainer(dbContainer, dataAttribute, value);
		}
		return row;
	}
	
	private void addNewDataRowToDbContainer(DataContainer dbContainer, DataAttribute dataAttribute, ArrayList<DataRow> value) {
		DataValue<ArrayList<DataRow>> newDataValue = new DataValue<>();
		newDataValue.setValue(LockLevel.RDU, value);

		DataRow links1 = new DataRow(dataAttribute, newDataValue);

		dbContainer.addAttributeValue(dataAttribute, links1);
	}
}
