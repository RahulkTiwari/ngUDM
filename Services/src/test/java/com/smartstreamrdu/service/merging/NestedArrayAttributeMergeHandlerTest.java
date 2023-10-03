package com.smartstreamrdu.service.merging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.CustomBigDecimal;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class NestedArrayAttributeMergeHandlerTest {
	
	private static final DataAttribute callRedemptions = DataAttributeFactory.getAttributeByNameAndLevel("callRedemptions", DataLevel.INS);
	private static final DataAttribute callId = DataAttributeFactory.getAttributeByNameAndLevelAndParent("callId", DataLevel.INS,callRedemptions);
	private static final DataAttribute callStartDate = DataAttributeFactory.getAttributeByNameAndLevelAndParent("callStartDate", DataLevel.INS,callRedemptions);
	private static final DataAttribute callSchedules = DataAttributeFactory.getAttributeByNameAndLevelAndParent("callSchedules", DataLevel.INS,callRedemptions);
	private static final DataAttribute callScheduleId = DataAttributeFactory.getAttributeByNameAndLevelAndParent("callScheduleId", DataLevel.INS,callSchedules);
	private static final DataAttribute callPrice = DataAttributeFactory.getAttributeByNameAndLevelAndParent("callPrice", DataLevel.INS,callSchedules);

	
	@Autowired
	private NestedArrayAttributeMergeHandler nestedArrayAttributeMergeHandler;
	
	@Autowired
	private BsonConverter convertorService;

	@Test 
	public void updateNestedArrayTest() throws Exception {
		
		DataContainer feedContainer = createDataContainer("NestedArrayUpdateTest/feedContainer.json");
		DataContainer dbContainer = createDataContainer("NestedArrayUpdateTest/dbContainer.json");
	
		DataRowIterator beforeUpdateiterator = new DataRowIterator(dbContainer, callRedemptions);
		
		LocalDate actualDate = LocalDate.of(2019, 9, 25);
		LocalDate beforeupdateDate = getCallStartDateForcallId("269663204",beforeUpdateiterator);
		assertEquals(beforeupdateDate, actualDate);
		
		nestedArrayAttributeMergeHandler.handleAttributeMerge(feedContainer,dbContainer, callRedemptions);
		
		DataRowIterator iterator = new DataRowIterator(dbContainer, callRedemptions);
		LocalDate updatedDate = getCallStartDateForcallId("269663204",iterator);
		LocalDate expectedDate = LocalDate.of(2023, 03, 14);
		assertEquals(expectedDate, updatedDate);
		
		DataRowIterator calliterator = new DataRowIterator(dbContainer, callRedemptions);

		CustomBigDecimal updatedCallPrice = getCallPriceForCallSechduleId("2018-02-16",calliterator);
		assertEquals(new CustomBigDecimal("333"), updatedCallPrice);

		DataRowIterator calliterator2 = new DataRowIterator(dbContainer, callRedemptions);

		CustomBigDecimal updatedCallPrice2 = getCallPriceForCallSechduleId("2020-01-29",calliterator2);
		assertEquals(new CustomBigDecimal("550"), updatedCallPrice2);

	}
	
	@Test
	public void insertNestedArrayTest() throws Exception {
		
		DataContainer feedContainer = createDataContainer("NestedArrayInsertTest/feedContainer.json");
		DataContainer dbContainer = createDataContainer("NestedArrayInsertTest/dbContainer.json");
	
		DataRowIterator iteratorBeforeUpdate = new DataRowIterator(dbContainer, callRedemptions);
		
		assertEquals(1,iteratorBeforeUpdate.stream().count());
		
		nestedArrayAttributeMergeHandler.handleAttributeMerge(feedContainer,dbContainer, callRedemptions);
		
		DataRowIterator iteratorAfterUpdate = new DataRowIterator(dbContainer, callRedemptions);

		assertEquals(2,iteratorAfterUpdate.stream().count());
		DataRowIterator iteratorUpdate = new DataRowIterator(dbContainer, callRedemptions);

		DataRow dataRow = getDataRow("269663204",iteratorUpdate);
		assertNotNull(dataRow);
		DataRowIterator iteratorCallSchedules = new DataRowIterator(dataRow, callSchedules);
		assertEquals(2,iteratorCallSchedules.stream().count());
	}
	
	@Test
	public void removeNestedArrayTest() throws Exception {
		
		DataContainer feedContainer = createDataContainer("NestedArrayRemoveTest/feedContainer.json");
		DataContainer dbContainer = createDataContainer("NestedArrayRemoveTest/dbContainer.json");
	
		DataRowIterator iteratorBeforeUpdate = new DataRowIterator(dbContainer, callRedemptions);
		
		assertEquals(3,iteratorBeforeUpdate.stream().count());
		
		nestedArrayAttributeMergeHandler.handleAttributeMerge(feedContainer,dbContainer, callRedemptions);
		
		DataRowIterator iteratorAfterUpdate = new DataRowIterator(dbContainer, callRedemptions);

		assertEquals(0,iteratorAfterUpdate.stream().count());
	}
	
	private DataRow getDataRow(String id, DataRowIterator iterator) {
		DataRow row = null;
		while(iterator.hasNext()) {
			DataRow dataRow = iterator.next();
			DataValue<Serializable> value = dataRow.getAttributeValue(callId);
			if(id.equals(value.getValue())) {
				row =  dataRow;
				break;
			}

		}
		return row;
	}

	private CustomBigDecimal getCallPriceForCallSechduleId(String id, DataRowIterator iteratorCallSchedules) {
		CustomBigDecimal price = null;
		while (iteratorCallSchedules.hasNext()) {
			DataRow dataRow = iteratorCallSchedules.next();
			DataRowIterator iterator = new DataRowIterator(dataRow, callSchedules);
			while (iterator.hasNext()) {
				DataRow row = iterator.next();
				DataValue<Serializable> value = row.getAttributeValue(callScheduleId);
				if (id.equals(value.getValue())) {
					price =  (CustomBigDecimal) row.getAttributeValue(callPrice).getValue();
					break;
				}
			}
			
		}
		return price;

	}

	private LocalDate getCallStartDateForcallId(String id, DataRowIterator iterator) {
		LocalDate date = null;
		while (iterator.hasNext()) {
			DataRow dataRow = iterator.next();
			DataValue<Serializable> value = dataRow.getAttributeValue(callId);
			if (id.equals(value.getValue())) {
				date = (LocalDate) dataRow.getAttributeValue(callStartDate).getValue();
				break;
			}
		}
		return date;
	}

	private DataContainer createDataContainer(String path) throws UdmTechnicalException, IOException {
		List<DataContainer> listOfDataContainers = convertorService.getListOfDataContainersFromFilePath(path,
				SdData.class);
		return listOfDataContainers.get(0);
	}

}
