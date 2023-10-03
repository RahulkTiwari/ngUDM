/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LoaderRuleExecutorTest.java
 * Author:	S Padgaonkar
 * Date:	04-Feb-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.junit.framework.BsonConverter;
import com.smartstreamrdu.persistence.domain.SdData;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@Profile("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class LoaderRuleExecutorTest  extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private BsonConverter bsonConverter;
	
	
	@Autowired
	private LoaderRuleExecutor executor;
	
	
	@Test
	public void testLoaderRuleExecutor_testActiveDataContainer() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"LoaderRuleExecutorTest/testActiveDataContainer/sdData.json", SdData.class);

		executor.executeLoaderRules(containers);

		DataContainer dataContainer = containers.get(0);
		DataAttribute inactivationDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel("rduInactivationDate",
				DataLevel.INS);
		LocalDate localDate = dataContainer.getHighestPriorityValue(inactivationDateAttribute);
        Assert.assertNotNull(localDate);
	}
	
	@Test
	public void testLoaderRuleExecutor_testInactiveDataContainer() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"LoaderRuleExecutorTest/testInactiveDataContainer/sdData.json", SdData.class);

		executor.executeLoaderRules(containers);

		DataContainer dataContainer = containers.get(0);
		DataAttribute inactivationDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel("rduInactivationDate",
				DataLevel.INS);
		LocalDate localDate = dataContainer.getHighestPriorityValue(inactivationDateAttribute);
        Assert.assertNull(localDate);
	}
	
	@Test(expected = NullPointerException.class)
	public void testLoaderRuleExecutor_testInvalidDataSource() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"LoaderRuleExecutorTest/testInvalidDataSource/sdData.json", SdData.class);

		executor.executeLoaderRules(containers);

		DataContainer dataContainer = containers.get(0);
		DataAttribute inactivationDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel("rduInactivationDate",
				DataLevel.INS);
		LocalDate localDate = dataContainer.getHighestPriorityValue(inactivationDateAttribute);
        Assert.assertNull(localDate);
        }
	
	@Test
	public void testLoaderRuleExecutor_testNormalizedInstrumentStatus() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"LoaderRuleExecutorTest/testNormalizedInstrumentStatus/sdData.json", SdData.class);

		executor.executeLoaderRules(containers);

		DataContainer dataContainer = containers.get(0);
		DataAttribute inactivationDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel("rduInactivationDate",
				DataLevel.INS);
		LocalDate localDate = dataContainer.getHighestPriorityValue(inactivationDateAttribute);
        Assert.assertNull(localDate);
       }
	
	@Test(expected = NullPointerException.class)
	public void testLoaderRuleExecutor_testInvalidDataSource_1() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"LoaderRuleExecutorTest/testInvalidDataSource_1/sdData.json", SdData.class);

		executor.executeLoaderRules(containers);

		DataContainer dataContainer = containers.get(0);
		DataAttribute inactivationDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel("rduInactivationDate",
				DataLevel.INS);
		LocalDate localDate = dataContainer.getHighestPriorityValue(inactivationDateAttribute);
        Assert.assertNull(localDate);
       }
	
	@Test
	public void testLoaderRuleExecutor_testIvoContainer() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"LoaderRuleExecutorTest/testIvoContainer/sdData.json", SdData.class);

		executor.executeLoaderRules(containers);

		DataContainer dataContainer = containers.get(0);
		DataAttribute inactivationDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel("rduInactivationDate",
				DataLevel.INS);
		LocalDate localDate = dataContainer.getHighestPriorityValue(inactivationDateAttribute);
        Assert.assertNull(localDate);
       }
	
	@Test
	public void testLoaderRuleExecutor_testLeDataContainer() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"LoaderRuleExecutorTest/testLeDataContainer/sdData.json", SdData.class);

		executor.executeLoaderRules(containers);

		DataContainer dataContainer = containers.get(0);
		DataAttribute inactivationDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel("rduInactivationDate",
				DataLevel.INS);
		LocalDate localDate = dataContainer.getHighestPriorityValue(inactivationDateAttribute);
        Assert.assertNull(localDate);
       }
	
	@Test(expected = IllegalArgumentException.class)
	public void testLoaderRuleExecutor_testNotApplicale() throws UdmTechnicalException, IOException {
	DataContainer container = new DataContainer(DataLevel.EXERCISE_STYLE_TYPES, null);

		executor.executeLoaderRules(Arrays.asList(container));

		DataAttribute inactivationDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel("rduInactivationDate",
				DataLevel.INS);
		LocalDate localDate = container.getHighestPriorityValue(inactivationDateAttribute);
        Assert.assertNull(localDate);
       }
	
	
	@Test
	public void testLoaderRuleExecutor_testNoChangeToHasChanged() throws UdmTechnicalException, IOException {
		List<DataContainer> containers = bsonConverter.getListOfDataContainersFromFilePath(
				"LoaderRuleExecutorTest/testNoChangeToHasChanged/sdData.json", SdData.class);
		executor.executeLoaderRules(containers);
		Assert.assertTrue(containers.get(0).hasContainerChanged());
		DataContainer dataContainer = containers.get(0);
		DataAttribute inactivationDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel("rduInactivationDate",
				DataLevel.INS);
		LocalDate localDate = dataContainer.getHighestPriorityValue(inactivationDateAttribute);
		Assert.assertNotNull(localDate);
		containers.get(0).setHasChanged(false);
		executor.executeLoaderRules(containers);
        Assert.assertTrue(!containers.get(0).hasContainerChanged());
	}
}
