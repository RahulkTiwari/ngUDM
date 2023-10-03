/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    FeedDataOverrideNestedArrayAttributeMergingService.java
 * Author:  Dedhia
 * Date:    16-Jul-2020
 *
 ********************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;

/**
 *  This merge strategy overrides the Feed data over the existing data in the database
 *  data container. Locks for all the nested structures that are still present  in the feed container
 *  will be retained. Any nested structures that are present in the database data container but not in the
 *  feed data container will be removed.
 *  
 * @author Dedhia
 *
 */
@Component("FeedDataOverrideNestedArrayAttributeMergingService")
public class FeedDataOverrideNestedArrayAttributeMergingService implements NestedArrayAttributeMergingService {
	
	
	@Autowired
	private ContainerComparatorFactory factory;

	@Autowired
	private DefaultNestedArrayAttributeMergingService defaultMergingService;
	
	private static final Logger _logger = LoggerFactory.getLogger(FeedDataOverrideNestedArrayAttributeMergingService.class);

	@Override
	public void merge(DataContainer feedContainer, DataContainer dbContainer, DataAttribute dataAttribute) {
		
		Objects.requireNonNull(feedContainer);
		Objects.requireNonNull(dbContainer);
		Objects.requireNonNull(dataAttribute);
		
		defaultMergingService.merge(feedContainer, dbContainer, dataAttribute);
		
		DataRowIterator dbIterator = new DataRowIterator(dbContainer, dataAttribute);

		NestedArrayComparator nestedArrayComparator = factory.getNestedArrayComparator(feedContainer);
		
		if(nestedArrayComparator == null){
			_logger.warn("No NestedArrayComparator found for {}. Hence not performing NestedArrayAttributeMergeHandler",feedContainer);
			return;
		}
		while(dbIterator.hasNext()){
			DataRow row = dbIterator.next();
			DataRowIterator feedContainerIterator = new DataRowIterator(feedContainer, dataAttribute);

			NestedArrayComparatorInputPojo inputPojo = NestedArrayComparatorInputPojo.builder().feed(row).dbIterator(feedContainerIterator)
					.feedDataContainer(feedContainer).dbDataContainer(dbContainer).arrayAttribute(dataAttribute).build();

			handleNestedArrayInDataRow(dbContainer, dbIterator, nestedArrayComparator, row, inputPojo);
		}
		
	}

	/**
	 * This method will remove data row from dbDataRow if no matching row found. If
	 * found than it iterate over the data row and check for nested array attributes
	 * and perform override operation on the dbDatarow.
	 * 
	 * @param dbContainer
	 * @param dbIterator
	 * @param nestedArrayComparator
	 * @param row
	 * @param inputPojo
	 */
	private void handleNestedArrayInDataRow(DataContainer dbContainer, DataRowIterator dbIterator,
			NestedArrayComparator nestedArrayComparator, DataRow row, NestedArrayComparatorInputPojo inputPojo) {
		DataRow matchingDataRow = nestedArrayComparator.compare(inputPojo);

		if (matchingDataRow == null) {
			// means there is no equivalent nested array available hence, remove the
			// attribute.
			dbIterator.remove();
		} else {
			overrideNestedArrayAttributeInRow(row, matchingDataRow, dbContainer, nestedArrayComparator);
		}
	}

	/**
	 * This method will override nested array feed data over the existing db data in
	 * DataRow.Any nested structures that are present in the database data row
	 * but not in the feed data row will be removed.
	 * 
	 * 
	 * @param row
	 * @param matchingDataRow
	 * @param dbContainer
	 * @param nestedArrayComparator
	 */
	private void overrideNestedArrayAttributeInRow(DataRow row, DataRow matchingDataRow, DataContainer dbContainer,
			NestedArrayComparator nestedArrayComparator) {
		Map<DataAttribute, DataValue<? extends Serializable>> nestedArrayAttributes = getNestedArrayAttribute(row);
		nestedArrayAttributes.forEach((k, v) -> {
			DataRowIterator rowIterator = new DataRowIterator(row, k);
			while (rowIterator.hasNext()) {
				DataRow dataRow = rowIterator.next();
				DataRowIterator iterator = new DataRowIterator(matchingDataRow, k);
				NestedArrayComparatorInputPojo inputPojo = NestedArrayComparatorInputPojo.builder().feed(dataRow)
						.dbIterator(iterator).dbDataContainer(dbContainer).arrayAttribute(k).build();

				handleNestedArrayInDataRow(dbContainer, rowIterator, nestedArrayComparator, dataRow, inputPojo);
			}
		});
	}

	/**
	 * This method will return map of nested Array DataAttributes from data row.
	 * 
	 * @param row
	 * @return
	 */
	private Map<DataAttribute, DataValue<? extends Serializable>> getNestedArrayAttribute(DataRow row) {
		return row.getRowData().entrySet().stream()
				.filter(entry -> DataType.NESTED_ARRAY.equals(entry.getKey().getDataType())).collect(Collectors
						.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, HashMap::new));
	}

}
