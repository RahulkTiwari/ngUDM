/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : EnReprocessingDataRetrievalServiceTest.java
 * Author :SaJadhav
 * Date : 07-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.spark.api.java.JavaRDD;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.message.ReprocessingData;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.spark.SparkUtil;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class EnReprocessingDataRetrievalServiceTest extends AbstractEmbeddedMongodbJunitParent {
	
	@Autowired
	private EnReprocessingDataRetrievalService enReprocessingDataRetrievalService;
	
	@Autowired
	private SparkUtil sparkUtil;
	
	@Value("${mongoDBUri}")
	private String mongoDbUri;

	private List<DataContainer> collect;
	
	@Before
	public  void initialize(){
	 System.setProperty("spark.mongodb.input.uri", mongoDbUri + ".sdData");
	}
	
	@Test
	@InputCollectionsPath(paths = {"EnReprocessingDataRetrievalServiceTest/input"})
	@ModifiedCollections(collections = {"enData"})
	public void testGetDataContainersToReprocess() throws UdmTechnicalException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources = new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.EN, Arrays.asList("rduEns"));
		Optional<JavaRDD<DataContainer>> dataContainersToReprocess = enReprocessingDataRetrievalService
				.getDataContainersToReprocess("eventTypes",
						createMapOfDSAndReprocessData("eventTypeMap",
								"ANTICIPATED FURTHER ADJUSTMENT/ANTICIPATED CASH SETTLEMENT", "OCC"),
						mapDataStorageVsDomainSources,"Cash Settlement");

		assertTrue(dataContainersToReprocess.isPresent());
		collect = dataContainersToReprocess.get().collect();
		assertEquals(2, collect.size());
	}
	
	@Test
	@InputCollectionsPath(paths = {"EnReprocessingDataRetrievalServiceTest/input"})
	@ModifiedCollections(collections = {"enData"})
	public void testGetDataContainersToReprocess_domainSourceTrdse() throws UdmTechnicalException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources = new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.EN, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> dataContainersToReprocess = enReprocessingDataRetrievalService
				.getDataContainersToReprocess("eventTypes",
						createMapOfDSAndReprocessData("eventTypeMap",
								"ANTICIPATED FURTHER ADJUSTMENT/ANTICIPATED CASH SETTLEMENT", "OCC"),
						mapDataStorageVsDomainSources,"Cash Settlement");

		assertTrue(dataContainersToReprocess.isPresent());
		collect = dataContainersToReprocess.get().collect();
		assertEquals(1, collect.size());
	}
	
	@Test
	@InputCollectionsPath(paths = {"EnReprocessingDataRetrievalServiceTest/input"})
	@ModifiedCollections(collections = {"enData"})
	public void testGetDataContainersToReprocess_domainSourceTrdse_withoutNormalizedValue() throws UdmTechnicalException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources = new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.EN, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> dataContainersToReprocess = enReprocessingDataRetrievalService
				.getDataContainersToReprocess("eventTypes",
						createMapOfDSAndReprocessData("eventTypeMap",
								"ANTICIPATED FURTHER ADJUSTMENT/ANTICIPATED CASH SETTLEMENT", "OCC"),
						mapDataStorageVsDomainSources,null);

		assertTrue(dataContainersToReprocess.isEmpty());
	}
	
	@Test
	@InputCollectionsPath(paths = {"EnReprocessingDataRetrievalServiceTest/input"})
	@ModifiedCollections(collections = {"enData"})
	public void testGetDataContainersToReprocess_nestedAttribute() throws UdmTechnicalException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources = new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.EN, Arrays.asList("rduEns"));
		Optional<JavaRDD<DataContainer>> dataContainersToReprocess = enReprocessingDataRetrievalService
				.getDataContainersToReprocess("currencyCodes",
						createMapOfDSAndReprocessData("currencyCodesMap",
								"Hk$", "HKFE"),
						mapDataStorageVsDomainSources,"HKFE");

		assertTrue(dataContainersToReprocess.isPresent());
		collect = dataContainersToReprocess.get().collect();
		assertEquals(2, collect.size());
	}
	
	@Test
	public void testGetDataContainersToReprocess_nonApplicableAttributes() throws UdmTechnicalException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources = new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.EN, Arrays.asList("rduEns"));
		Optional<JavaRDD<DataContainer>> dataContainersToReprocess = enReprocessingDataRetrievalService
				.getDataContainersToReprocess("tenors",
						createMapOfDSAndReprocessData("currencyCodesMap",
								"Hk$", "HKFE"),
						mapDataStorageVsDomainSources,null);

		assertTrue(dataContainersToReprocess.isEmpty());
	}
	
	@Test
	public void testGetDataContainersToReprocess_nonApplicableDomainSources() throws UdmTechnicalException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources = new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.SD, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> dataContainersToReprocess = enReprocessingDataRetrievalService
				.getDataContainersToReprocess("currencyCodes",
						createMapOfDSAndReprocessData("currencyCodesMap",
								"Hk$", "HKFE"),
						mapDataStorageVsDomainSources,"");
		assertTrue(dataContainersToReprocess.isEmpty());
	}
	
	private Map<String, List<ReprocessingData>> createMapOfDSAndReprocessData(String domainName,String val1,String val2){
		Map<String, List<ReprocessingData>> map = new HashMap<>();
		map.put("rduEns", createReprocessingDataList(domainName, val1, val2));
		return map;
	}
	
	private List<ReprocessingData> createReprocessingDataList(String domainName,String val1,String val2){
		List<ReprocessingData> reprocessDataList = new ArrayList<>();
		
		ReprocessingData rd = new ReprocessingData();
		rd.setDomainName(domainName);
		rd.setDomainSource("rduEns");
		DomainType dt = new DomainType(val1, val2, null, domainName);
		rd.setDomainType(dt);
		reprocessDataList.add(rd);
		return reprocessDataList;
	}
	
	@After
	public void destroy(){
		if(!sparkUtil.getSparkContext().sparkContext().isStopped()){
			sparkUtil.getSparkContext().close();
		}
	}

}
