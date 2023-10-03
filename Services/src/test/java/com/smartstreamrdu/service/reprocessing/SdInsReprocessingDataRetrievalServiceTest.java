package com.smartstreamrdu.service.reprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
import com.smartstreamrdu.persistence.mongodb.EmbeddedMongoConfigTest;
import com.smartstreamrdu.service.spark.SparkUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { EmbeddedMongoConfigTest.class })
public class SdInsReprocessingDataRetrievalServiceTest extends AbstractEmbeddedMongodbJunitParent{
	
	@Autowired
	private SdInsReprocessingDataRetrievalService sdInsReprocessRetrievalService;
	
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
	@InputCollectionsPath(paths={"ReprocessingDataRetrievalServiceTest/testGetSecuritiesForSecLevelAttr/input"})
	@ModifiedCollections(collections={"sdData"})
	public void testGetSecuritiesForSecLevelAttr() throws UdmTechnicalException, ClassNotFoundException, IOException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources=new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.SD, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> rdd = sdInsReprocessRetrievalService.getDataContainersToReprocess("currencyCodes", createMapOfDSAndReprocessData(), mapDataStorageVsDomainSources,null);
		assertTrue(rdd.isPresent());
		
		List<DataContainer> reprocessSecurityList = rdd.get().collect();
		assertEquals(1, reprocessSecurityList.size());
	}
	
	@Test
	@InputCollectionsPath(paths={"ReprocessingDataRetrievalServiceTest/testGetSecuritiesForInsLevelAttr/input"})
	@ModifiedCollections(collections={"sdData"})
	public void testGetSecuritiesForInsLevelAttr() throws UdmTechnicalException, ClassNotFoundException, IOException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources=new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.SD, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> rdd = sdInsReprocessRetrievalService.getDataContainersToReprocess("currencyCodes", createMapOfDSAndReprocessData(), mapDataStorageVsDomainSources,null);
		assertTrue(rdd.isPresent());
		collect = rdd.get().collect();
		assertEquals(1, collect.size());
		DataContainer dataContainer = collect.get(0);
		assertEquals(2, dataContainer.getAllChildDataContainers().size());
	}
	
	@Test
	@InputCollectionsPath(paths={"ReprocessingDataRetrievalServiceTest/testGetMultipleSecuritiesForSecLevelAttr/input"})
	@ModifiedCollections(collections={"sdData"})
	public void testGetMultipleSecuritiesForSecLevelAttr() throws UdmTechnicalException, ClassNotFoundException, IOException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources=new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.SD, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> rdd = sdInsReprocessRetrievalService.getDataContainersToReprocess("currencyCodes", createMapOfDSAndReprocessDataMultipleSecurities(), mapDataStorageVsDomainSources,null);
		assertTrue(rdd.isPresent());
		
		List<DataContainer> reprocessSecurityList = rdd.get().collect();
		assertEquals(2, reprocessSecurityList.size());
	}
	
	@Test
	@InputCollectionsPath(paths= {"ReprocessingDataRetrievalServiceTest/testGetOnlyActiveSecurities/input"})
	@ModifiedCollections(collections= {"sdData"})
	public void testGetOnlyActiveSecurities() throws UdmTechnicalException, ClassNotFoundException, IOException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources=new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.SD, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> rdd = sdInsReprocessRetrievalService.getDataContainersToReprocess("currencyCodes", createMapOfDSAndReprocessDataMultipleSecurities(), mapDataStorageVsDomainSources,null);
		assertTrue(rdd.isPresent());
		
		List<DataContainer> reprocessSecurityList = rdd.get().collect();
		assertEquals(1, reprocessSecurityList.size());
	}
	
	@Test
	@InputCollectionsPath(paths= {"ReprocessingDataRetrievalServiceTest/testSearchforExchangeCodesDomain/input"})
	@ModifiedCollections(collections= {"sdData"})
	public void testSearchforExchangeCodesDomain() throws UdmTechnicalException, ClassNotFoundException, IOException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources=new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.SD, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> rdd = sdInsReprocessRetrievalService.getDataContainersToReprocess("exchangeCodes", createMapOfDSAndReprocessDataForExchnageCodes("EXPM"), mapDataStorageVsDomainSources,"EXPM");
		assertTrue(rdd.isPresent());
		
		List<DataContainer> reprocessSecurityList = rdd.get().collect();
		assertEquals(3, reprocessSecurityList.size());
	}
	
	@Test
	@InputCollectionsPath(paths= {"ReprocessingDataRetrievalServiceTest/testSearchforExchangeCodesDomain/input"})
	@ModifiedCollections(collections= {"sdData"})
	public void testSearchforExchangeCodesDomain_noresult() throws UdmTechnicalException, ClassNotFoundException, IOException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources=new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.SD, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> rdd = sdInsReprocessRetrievalService.getDataContainersToReprocess("exchangeCodes", createMapOfDSAndReprocessDataForExchnageCodes("XNYS"), mapDataStorageVsDomainSources,null);
		assertTrue(rdd.isPresent());
		
		List<DataContainer> reprocessSecurityList = rdd.get().collect();
		assertEquals(0, reprocessSecurityList.size());
	}
	
	@Test
	public void testNonApplicableAttributes() throws UdmTechnicalException, ClassNotFoundException, IOException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources = new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.SD, Arrays.asList("trds"));
		Optional<JavaRDD<DataContainer>> rdd = sdInsReprocessRetrievalService.getDataContainersToReprocess("eventTypes",
				createMapOfDSAndReprocessDataForExchnageCodes("XNYS"), mapDataStorageVsDomainSources,null);
		assertFalse(rdd.isPresent());
	}
	
	@Test
	public void testNonApplicableDomainSources() throws UdmTechnicalException, ClassNotFoundException, IOException{
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources = new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.EN, Arrays.asList("rduEns"));
		Optional<JavaRDD<DataContainer>> rdd = sdInsReprocessRetrievalService.getDataContainersToReprocess("exchangeCodes",
				createMapOfDSAndReprocessDataForExchnageCodes("XNYS"), mapDataStorageVsDomainSources,null);
		assertFalse(rdd.isPresent());
	}
	
	
	@After
	public void destroy(){
		if(!sparkUtil.getSparkContext().sparkContext().isStopped()){
			sparkUtil.getSparkContext().close();
		}
	}
	
	/**
	 * @return
	 */
	private Map<String, List<ReprocessingData>> createMapOfDSAndReprocessDataForExchnageCodes(String exchangeCodeVal) {
		Map<String, List<ReprocessingData>> map = new HashMap<>();
		List<ReprocessingData> reprocessDataList = new ArrayList<>();

		ReprocessingData rd = new ReprocessingData();
		rd.setDomainName("marketSegmentMicMap");
		rd.setDomainSource("trds");
		DomainType dt = new DomainType(exchangeCodeVal, null, null, "marketSegmentMicMap");
		rd.setDomainType(dt);
		reprocessDataList.add(rd);
		map.put("trds", reprocessDataList);
		return map;

	}

	private Map<String, List<ReprocessingData>> createMapOfDSAndReprocessData(){
		Map<String, List<ReprocessingData>> map = new HashMap<>();
		map.put("trds", createReprocessingDataList());
		return map;
	}
	
	
	@Test
	@InputCollectionsPath(paths={"ReprocessingDataRetrievalServiceTest/testRegexAttribute/input"})
	@ModifiedCollections(collections={"sdData"})
	public void testRegexAttribute() throws UdmTechnicalException, ClassNotFoundException, IOException {
		Map<DataStorageEnum, List<String>> mapDataStorageVsDomainSources=new EnumMap<>(DataStorageEnum.class);
		mapDataStorageVsDomainSources.put(DataStorageEnum.SD, Arrays.asList("trds","idcApex"));

		Optional<JavaRDD<DataContainer>> rdd = sdInsReprocessRetrievalService.getDataContainersToReprocess("xrfAdditionalTypes", createReprocessingDataListXrfAdditional(), mapDataStorageVsDomainSources,null);
		assertTrue(rdd.isPresent());
		
		List<DataContainer> reprocessSecurityList = rdd.get().collect();
		assertEquals(2, reprocessSecurityList.size());
	}
	
	private Map<String, List<ReprocessingData>>  createReprocessingDataListXrfAdditional(){
		List<ReprocessingData> reprocessDataList = new ArrayList<>();
		Map<String, List<ReprocessingData>> map = new HashMap<>();
		
		ReprocessingData rd = new ReprocessingData();
		rd.setDomainName("xrfAdditionalTypesMap");
		rd.setDomainSource("idcApex");
		DomainType dt = new DomainType(".*e$", "CHIX", null, "xrfAdditionalTypesMap");
		rd.setDomainType(dt);
		
		reprocessDataList.add(rd);
		
		List<ReprocessingData> reprocessDataList1 = new ArrayList<>();

		ReprocessingData rd1 = new ReprocessingData();
		rd1.setDomainName("xrfAdditionalTypesMap");
		rd1.setDomainSource("trds");
		DomainType dt1 = new DomainType(".*BA", "BUE", null, "xrfAdditionalTypesMap");
		rd1.setDomainType(dt1);
		
		reprocessDataList1.add(rd1);
		map.put("idcApex", reprocessDataList);
		map.put("trds", reprocessDataList1);

		return map;
		
	}
	private List<ReprocessingData> createReprocessingDataList(){
		List<ReprocessingData> reprocessDataList = new ArrayList<>();
		
		ReprocessingData rd = new ReprocessingData();
		rd.setDomainName("currencyCodesMap");
		rd.setDomainSource("trds");
		DomainType dt = new DomainType("AFN", null, null, "currencyCodesMap");
		rd.setDomainType(dt);
		reprocessDataList.add(rd);
		return reprocessDataList;
	}
	
	private Map<String, List<ReprocessingData>> createMapOfDSAndReprocessDataMultipleSecurities(){
		Map<String, List<ReprocessingData>> map = new HashMap<>();
		map.put("trds", createReprocessingDataListMultipleSecurities());
		return map;
	}
	
	
	private List<ReprocessingData> createReprocessingDataListMultipleSecurities() {
		List<ReprocessingData> reprocessDataList = new ArrayList<>();

		ReprocessingData rd1 = new ReprocessingData();
		rd1.setDomainName("currencyCodesMap");
		rd1.setDomainSource("trds");
		DomainType dt1 = new DomainType("AFN", null, null, "currencyCodesMap");
		rd1.setDomainType(dt1);
		
		ReprocessingData rd2 = new ReprocessingData();
		rd2.setDomainName("currencyCodesMap");
		rd2.setDomainSource("trds");
		DomainType dt2 = new DomainType("AED", null, null, "currencyCodesMap");
		rd2.setDomainType(dt2);
		
		reprocessDataList.add(rd1);
		reprocessDataList.add(rd2);
		return reprocessDataList;
	}
}
