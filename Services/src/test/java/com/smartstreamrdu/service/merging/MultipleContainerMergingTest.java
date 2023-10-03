/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: MultipleContainerMergingTest.java
 * Author: Shruti Arora
 * Date: 29-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.PersistenceService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class MultipleContainerMergingTest extends CrossRefDataContainers {

	@Autowired
	AttributeMergingDecorator attrDecorator;

	@Autowired
	PersistenceService persist;

	@Test
	public void test_XRfChildCompartor() throws Exception {

		DataContainer container1 = getDataContainer1();
		DataContainer container2 = getDataContainer2();
		DataContainer container3 = getDataContainerWithTwoXRSec();

		DataContainerMergingDecorator dcDecorator = new DataContainerMergingDecorator(attrDecorator);
		MultipleContainerMergingDecorator decorator = new MultipleContainerMergingDecorator(dcDecorator);

		DataAttribute insStatus = DataAttributeFactory.getAttributeByNameAndLevel("xrfInstrumentStatus",
				DataLevel.XRF_INS);
		DataAttribute secStatus = DataAttributeFactory.getAttributeByNameAndLevel("xrfSecurityStatus",
				DataLevel.XRF_SEC);

		Assert.assertEquals(1, container1.getChildDataContainers(DataLevel.XRF_SEC).size());

		merge(decorator, container1, createList(container1, container2, container3));

		Assert.assertEquals(3, container1.getChildDataContainers(DataLevel.XRF_SEC).size());
		DomainType value = (DomainType) container2.getAttributeValueAtLevel(LockLevel.RDU, insStatus);
		Assert.assertEquals("D", value.getNormalizedValue());
		DomainType value2 = (DomainType) container3.getAttributeValueAtLevel(LockLevel.RDU, insStatus);
		Assert.assertEquals("D", value2.getNormalizedValue());
		DomainType value3 = (DomainType) container1.getAttributeValueAtLevel(LockLevel.RDU, insStatus);
		Assert.assertEquals("A", value3.getNormalizedValue());

		List<DataContainer> childContainers = container1.getChildDataContainers(DataLevel.XRF_SEC);
		List<DataContainer> childContainers2 = container2.getChildDataContainers(DataLevel.XRF_SEC);
		List<DataContainer> childContainers3 = container3.getChildDataContainers(DataLevel.XRF_SEC);
		childContainers.forEach(x -> {
			DomainType val = (DomainType) x.getAttributeValueAtLevel(LockLevel.RDU, secStatus);
			Assert.assertEquals("A", val.getNormalizedValue());
		});
		childContainers2.forEach(x -> {
			DomainType val = (DomainType) x.getAttributeValueAtLevel(LockLevel.RDU, secStatus);
			Assert.assertEquals("D", val.getNormalizedValue());
		});
		childContainers3.forEach(x -> {
			DomainType val = (DomainType) x.getAttributeValueAtLevel(LockLevel.RDU, secStatus);
			Assert.assertEquals("D", val.getNormalizedValue());
		});

		merge(decorator, container1, null);

		DomainType value4 = (DomainType) container1.getAttributeValueAtLevel(LockLevel.RDU, insStatus);
		Assert.assertEquals("A", value4.getNormalizedValue());
		List<DataContainer> childContainers4 = container1.getChildDataContainers(DataLevel.XRF_SEC);
		childContainers4.forEach(x -> {
			DomainType val = (DomainType) x.getAttributeValueAtLevel(LockLevel.RDU, secStatus);
			Assert.assertEquals("A", val.getNormalizedValue());
		});

		DataContainer withOutSecs = getContainerWithoutSecurity();
		DataContainer withOutSecs1 = getContainerWithoutSecurity();

		withOutSecs.set_id("withOutS1");
		withOutSecs1.set_id("withOutS2");
		merge(decorator, withOutSecs, createList(container1, withOutSecs1));

		DomainType value5 = (DomainType) withOutSecs1.getAttributeValueAtLevel(LockLevel.RDU, insStatus);
		Assert.assertEquals("D", value5.getNormalizedValue());
		DomainType value51 = (DomainType) container1.getAttributeValueAtLevel(LockLevel.RDU, insStatus);
		Assert.assertEquals("A", value51.getNormalizedValue());
		List<DataContainer> childContainers41 = container1.getChildDataContainers(DataLevel.XRF_SEC);
		childContainers41.forEach(x -> {
			DomainType val = (DomainType) x.getAttributeValueAtLevel(LockLevel.RDU, secStatus);
			Assert.assertEquals("A", val.getNormalizedValue());
		});

		DataContainer container4 = getDataContainer1();
		merge(decorator, container4, createList(withOutSecs1, container4));

		DomainType value6 = (DomainType) container4.getAttributeValueAtLevel(LockLevel.RDU, insStatus);
		Assert.assertEquals("D", value6.getNormalizedValue());

		DataContainer container5 = getDataContainer1();
		container5.set_id("container1");
		sleep();
		DataContainer withOutLinks = getDataContainerwithoutLinks2();
		withOutLinks.set_id("withOutL1");

		List<DataContainer> list = new ArrayList<>();
		list.add(container5);
		list.add(withOutLinks);

		merge(decorator, withOutSecs, list);

		DomainType value7 = (DomainType) withOutLinks.getAttributeValueAtLevel(LockLevel.RDU, insStatus);
		Assert.assertEquals("D", value7.getNormalizedValue());
		DomainType value8 = (DomainType) container5.getAttributeValueAtLevel(LockLevel.RDU, insStatus);
		Assert.assertEquals("A", value8.getNormalizedValue());

	}

	private List<DataContainer> createList(DataContainer... in) {
		return new ArrayList<>(Arrays.asList(in));
	}

	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void merge(MultipleContainerMergingDecorator decorator, DataContainer feedContainer,
			List<DataContainer> dbContainers) throws Exception {
			decorator.merge(feedContainer, dbContainers);
	}

}
