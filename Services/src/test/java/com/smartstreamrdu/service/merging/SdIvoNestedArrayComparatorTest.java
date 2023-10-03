/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: SdIvoNestedArrayComparatorTest.java
* Author : VRamani
* Date : Apr 24, 2019
* 
*/
package com.smartstreamrdu.service.merging;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
* @author VRamani
*
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class SdIvoNestedArrayComparatorTest {

	@Autowired
	SdIvoNestedArrayComparator comparator;
	
	@Test
	public void testCompare() {

		DataAttribute insinsLink = DataAttributeFactory.getIvoRelationAttributeForInsAndIns();
		DataRow ref1 = new DataRow(insinsLink);
		DataValue<String> val = new DataValue<>();
		String insUniqueId2 = UUID.randomUUID().toString();
		val.setValue(LockLevel.FEED, insUniqueId2);
		DataAttribute refDataAttr = DataAttributeFactory.getRelationRefDataAttribute(insinsLink);
		ref1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("trAssetId", DataLevel.IVO_INS), val);
		ref1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("nameLong", DataLevel.IVO_INS), val);
		ref1.addAttribute(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.IVO_INS), val);
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(insinsLink);
		DataRow link1 = new DataRow(insinsLink);
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.RDU, RelationType.Underlying.getRelationTypeName());
		link1.addAttribute(relationType, value);

		link1.addAttribute(refDataAttr, ref1);

		ArrayList<DataRow> linkList1 = new ArrayList<>();

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.RDU, linkList1);
		linkList1.add(link1);

		DataRow r = new DataRow(insinsLink, valueList1);

		DataContainer con = DataContainerTestUtil.getDataContainer(DataLevel.IVO_INS);

		con.addAttributeValue(insinsLink, r);

		DataRowIterator feediterator = new DataRowIterator(con, insinsLink);
		
		NestedArrayComparatorInputPojo inputPojo = NestedArrayComparatorInputPojo.builder().feed(link1).dbIterator(feediterator)
			.arrayAttribute(insinsLink).build();

		DataRow compare = comparator.compare(inputPojo);

		Assert.assertEquals(link1, compare);

	}
}
