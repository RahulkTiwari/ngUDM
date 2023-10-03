package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.cache.initializer.IgniteInitializer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MergingTestConfiguration.class })
public class FeedDataOverrideNestedArrayAttributeMergingServiceTest  extends AbstractEmbeddedMongodbJunitParent {
	
	private static final DataAttribute parentAttribute = DataAttributeFactory.getAttributeByNameAndLevel("instrumentSnpRatings", DataLevel.INS);
	
	@Autowired
	private FeedDataOverrideNestedArrayAttributeMergingService service;
	
	@Autowired
	IgniteInitializer igniteInitializer;

	@Test
	@SuppressWarnings("unchecked")
	@InputCollectionsPath(paths={"FeedDataOverrideNestedArrayAttributeMergingServiceTest"})
	@ModifiedCollections(collections={"dvDomainMap"})
	public void testMerge() {
		
		igniteInitializer.reloadCache();
		
		DataContainer feedContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		DataContainer dbContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		
		DomainType dt1 = new DomainType();
		dt1.setVal("FCLONG");
		DataValue<DomainType> dataVal1 = new DataValue<>();
		dataVal1.setValue(LockLevel.FEED, dt1);
		
		DomainType dt2 = new DomainType();
		dt2.setVal("STDLONG");
		DataValue<DomainType> dataVal2 = new DataValue<>();
		dataVal2.setValue(LockLevel.FEED, dt2);
		
		DataAttribute insinsLink = parentAttribute;
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, "VALUE_1");
		DataRow link1 = new DataRow(insinsLink);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevelAndParent("insSnpRatingSymbol", DataLevel.INS, parentAttribute), val);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevelAndParent("insSnpRatingTypeCode", DataLevel.INS, parentAttribute), dataVal1);
		ArrayList<DataRow> linkList1 = new ArrayList<>();
		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		linkList1.add(link1);
		DataRow r = new DataRow(insinsLink, valueList1);
		feedContainer.addAttributeValue(insinsLink, r);
		
		
		DataAttribute insinsLink1 = parentAttribute;
		DataValue<String> val1 = new DataValue<>();
		val1.setValue(LockLevel.FEED, "VALUE_2");
		DataRow link11 = new DataRow(insinsLink1);
		link11.addAttribute(DataAttributeFactory.getAttributeByNameAndLevelAndParent("insSnpRatingSymbol", DataLevel.INS, parentAttribute), val1);
		link11.addAttribute(DataAttributeFactory.getAttributeByNameAndLevelAndParent("insSnpRatingTypeCode", DataLevel.INS, parentAttribute), dataVal2);
		ArrayList<DataRow> linkList11 = new ArrayList<>();
		DataValue<ArrayList<DataRow>> valueList11 = new DataValue<>();
		valueList11.setValue(LockLevel.FEED, linkList11);
		linkList11.add(link11);
		DataRow r1 = new DataRow(insinsLink1, valueList11);
		dbContainer.addAttributeValue(insinsLink1, r1);
		
		
		DataAttribute dsAttribute = DataAttributeFactory.getDatasourceAttribute(DataLevel.INS);
		
		DomainType dt = new DomainType();
		dt.setVal("snpXfr");
		DataValue<DomainType> dataValue = new DataValue<>();
		dataValue.setValue(LockLevel.FEED, dt);
		
		feedContainer.addAttributeValue(dsAttribute, dataValue);
		dbContainer.addAttributeValue(dsAttribute, dataValue);
		
		service.merge(feedContainer, dbContainer, parentAttribute);
		
		DataRow attribueValue = (DataRow) dbContainer.getAttributeValue(parentAttribute);
		Serializable value = attribueValue.getValue().getValue();
		List<DataRow> dataRows = (List<DataRow>) value;
		
		DataRow attribueValue1 = (DataRow) feedContainer.getAttributeValue(parentAttribute);
		Serializable value1 = attribueValue1.getValue().getValue();
		List<DataRow> dataRows1 = (List<DataRow>) value1;
		
		Assert.assertEquals(1, dataRows.size());
		Assert.assertEquals(1, dataRows1.size());
		
		DataRow feedRow = dataRows1.get(0);
		DataRow dbRow = dataRows.get(0);
		
		Serializable feedValue = feedRow.getAttributeValue(DataAttributeFactory.getAttributeByNameAndLevelAndParent("insSnpRatingSymbol", DataLevel.INS, parentAttribute));
		Serializable dbValue = dbRow.getAttributeValue(DataAttributeFactory.getAttributeByNameAndLevelAndParent("insSnpRatingSymbol", DataLevel.INS, parentAttribute));
		Assert.assertEquals(feedValue, dbValue);

		DataValue<Serializable> dbValueForCode = dbRow.getAttributeValue(DataAttributeFactory.getAttributeByNameAndLevelAndParent("insSnpRatingSymbol", DataLevel.INS, parentAttribute));
		Assert.assertEquals("VALUE_1", dbValueForCode.getValue());
	}

}
