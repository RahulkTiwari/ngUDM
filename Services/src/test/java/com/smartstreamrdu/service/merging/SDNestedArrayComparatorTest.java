/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	SDNestedArrayComparatorTest.java
 * Author:	Jay Sangoi
 * Date:	11-Sep-2018
 *
 *******************************************************************
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
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class SDNestedArrayComparatorTest {

	@Autowired
	private SDNestedArrayComparator comparator;

	@Test
	public void test_compare() {

		DataAttribute insinsLink = DataAttributeFactory.getRelationAttributeForInsAndIns();
		DataRow ref1 = new DataRow(insinsLink);
		DataValue<String> val = new DataValue<>();
		String insUniqueId2 = UUID.randomUUID().toString();
		val.setValue(LockLevel.FEED, insUniqueId2);
		DataAttribute refDataAttr = DataAttributeFactory.getRelationRefDataAttribute(insinsLink);
		ref1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("trAssetId", DataLevel.INS), val);
		ref1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("nameLong", DataLevel.INS), val);
		ref1.addAttribute(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS), val);
		DataAttribute relationType = DataAttributeFactory.getRelationTypeAttribute(insinsLink);
		DataRow link1 = new DataRow(insinsLink);
		DataValue<String> value = new DataValue<>();
		value.setValue(LockLevel.FEED, RelationType.Underlying.getRelationTypeName());
		link1.addAttribute(relationType, value);

		link1.addAttribute(refDataAttr, ref1);

		ArrayList<DataRow> linkList1 = new ArrayList<>();

		DataValue<ArrayList<DataRow>> valueList1 = new DataValue<>();
		valueList1.setValue(LockLevel.FEED, linkList1);
		linkList1.add(link1);

		DataRow r = new DataRow(insinsLink, valueList1);

		DataContainer con = DataContainerTestUtil.getDataContainer(DataLevel.INS);

		con.addAttributeValue(insinsLink, r);
		
		DomainType dt = new DomainType();
		dt.setVal("trdse");
		DataValue<DomainType> dv = new DataValue<>();
		dv.setValue(LockLevel.FEED, dt);
		
		con.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), dv);

		DataRowIterator feediterator = new DataRowIterator(con, insinsLink);
		
		NestedArrayComparatorInputPojo inputPojo = NestedArrayComparatorInputPojo.builder().feed(link1).dbIterator(feediterator)
				.arrayAttribute(insinsLink).dbDataContainer(con).build();

		DataRow compare = comparator.compare(inputPojo);

		Assert.assertEquals(link1, compare);

	}

}
