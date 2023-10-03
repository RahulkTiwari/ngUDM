/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataServiceImplTest.java
 * Author:	Divya Bharadwaj
 * Date:	19-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.util.SdRawDataConstant;

/**
 * @author Bharadwaj
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class RawDataServiceImplTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	RawDataService rawDataService;
	
	Record record;
	String dataSource;
	List<String> deltaFieldsByParser;
	String sourceUniqueIdAttributeName;
	String feedExecutionDetailId;
	String rawLevel;
	String fileType;
	String sourceUniqueIdValue;
	List<Record> _id;
	@Autowired
	private transient MongoTemplate mongoTemplate;
	
	@Before
	public void init(){
		record=new Record();
		record.setAttribute("$.isin", "testIsin");
		record.getRecordRawData().setRawAttribute("$.isin", "testIsin");
		dataSource="trdse";
		deltaFieldsByParser=new ArrayList<>();
		deltaFieldsByParser.add("isin");
		sourceUniqueIdAttributeName="isin";
		feedExecutionDetailId="1";
		rawLevel="SEC";
		fileType="XE";
		sourceUniqueIdValue="232769515109";
	}
	@Test
	public void testRawDataService(){
		List<RawRecordContextDetailsPojo> pojoList = getListOfPojo(record, dataSource, null, sourceUniqueIdAttributeName, feedExecutionDetailId, rawLevel, fileType,null,sourceUniqueIdValue);
		_id=rawDataService.handleRawData(pojoList, null,new RawDataFullLoadFilter(),null,null,null);
		Assert.assertNotNull(_id);
	}
	
	@Test
	@ModifiedCollections(collections = {"sdRawData"})
	@InputCollectionsPath(paths = {"RawDataServiceTest/sdRawData.json"})
	public void testRawDataService_idcApex_dataSource(){
		String dataSource="idcApex";
		String fileType = "IDC APEX GSM Equity";
		String rawLevel="INS";
		String sourceUniqueIdAttributeName=null;
		String rawDataId = "63b40ec19eb4512be6a06b63";
		Record record=new Record();
		record.setParserName("globalSecurityMasterEquity");
		record.setMessageType("globalSecurityMasterEquity");
		List<RawRecordContextDetailsPojo> pojoList = getListOfPojo(record, dataSource, null, sourceUniqueIdAttributeName, null, rawLevel, fileType,null,"17036211");
		_id=rawDataService.handleRawData(pojoList, null,new RawDataDeltaProcessingFilter(),null,null,null);
		Assert.assertNotNull(_id);
		Assert.assertEquals(rawDataId, _id.get(0).getRecordRawData().getRawDataId());
		Assert.assertEquals(fileType, _id.get(0).getFileType());
		
	}
	
	@Test
	public void testRawDataServiceException(){
		record.getRecordRawData().setRawData(null);
		List<RawRecordContextDetailsPojo> pojoList = getListOfPojo(record, dataSource, null, sourceUniqueIdAttributeName, feedExecutionDetailId, rawLevel, fileType,null,sourceUniqueIdValue);
		_id=rawDataService.handleRawData(pojoList, null,new RawDataFullLoadFilter(),null,null,null);
		Assert.assertNotNull(_id);
	}
	
	/**
	 * @param record2
	 * @param dataSource2
	 * @param object
	 * @param sourceUniqueIdAttributeName2
	 * @param feedExecutionDetailId2
	 * @param rawLevel2
	 * @param fileType2
	 * @param object2
	 * @return
	 */
	private List<RawRecordContextDetailsPojo> getListOfPojo(Record record2, String dataSource2, List<String> object,
			String sourceUniqueIdAttributeName2, String feedExecutionDetailId2, String rawLevel2, String fileType2,
			Object object2,String sourceUniqueIdValue) {
		RawRecordContextDetailsPojo pojo = new RawRecordContextDetailsPojo();
		pojo.setDataSource(dataSource2);
		pojo.setDeltaFieldsByParser(object);
		pojo.setFeedExecutionDetailId(feedExecutionDetailId2);
		pojo.setFileType(fileType2);
		pojo.setRawDataLevelForParser(rawLevel2);
		pojo.setRecord(record2);
		pojo.setSourceUniqueIdAttributeName(sourceUniqueIdAttributeName2);
		pojo.setSourceUniqueIdAttributeValue(sourceUniqueIdValue);
		pojo.setDeltaProcessingFlag(false);
		
		
		return Arrays.asList(pojo);
	}
	@Test
	public void testRawDataServiceForUpdate(){
		List<RawRecordContextDetailsPojo> pojoList = getListOfPojo(record, dataSource, null, sourceUniqueIdAttributeName, feedExecutionDetailId, rawLevel, fileType,null,sourceUniqueIdValue);
		_id=rawDataService.handleRawData(pojoList, null,new RawDataFullLoadFilter(),null, null,null);
		//record.getRawData().put("ric", "testRic");
		record.getRecordRawData().setRawAttribute("ric", "testRic");
		Assert.assertNotNull(rawDataService.handleRawData(pojoList, null,new RawDataFullLoadFilter(),null,null,null));
		
	}
	
	@Test
	public void testRawDataServiceForEmptyDeltaFields(){
		List<RawRecordContextDetailsPojo> pojoList = getListOfPojo(record, dataSource, null, sourceUniqueIdAttributeName, feedExecutionDetailId, rawLevel, fileType,null,sourceUniqueIdValue);
		_id=rawDataService.handleRawData(pojoList, null,new RawDataFullLoadFilter(),null,null,null);
		Assert.assertNotNull(_id);
		
	}
	
	@Test
	public void testRawDataServiceForRawReProcessing(){
		String dataSource="rdsp";
		String fileType = "CONSOLIDATED_INS|"+SdRawDataConstant.RAW_REPROCESSING_FILETYPE;
		String rawDataLevel="INS";
		String rawDataId = "601cddd7985ee806b5c3ae96";
		record.getRecordRawData().setRawAttribute(SdRawDataConstant.RAW_DATA_ID, rawDataId);
		List<RawRecordContextDetailsPojo> pojoList = getListOfPojo(record, dataSource, null, sourceUniqueIdAttributeName, feedExecutionDetailId, rawDataLevel,fileType,null,sourceUniqueIdValue);
		_id=rawDataService.handleRawData(pojoList, null,new RawDataFullLoadFilter(),null, null,null);
		Assert.assertEquals(rawDataId, _id.get(0).getRecordRawData().getRawDataId());
		Assert.assertNull(_id.get(0).getRecordRawData().getRawData().get(SdRawDataConstant.RAW_DATA_ID));
		record.getRecordRawData().setRawAttribute("nameShort", "FX_RT due 2021");
		Assert.assertNotNull(rawDataService.handleRawData(pojoList, null,new RawDataFullLoadFilter(),null,null,null));
	}
	
	@After
	public void dropRawData(){
		mongoTemplate=SpringUtil.getBean(MongoTemplate.class);
		Query query=new Query();
		query.addCriteria(Criteria.where("_id").is(record.getRecordRawData().getRawDataId()));
		mongoTemplate.remove(query, "sdRawData");
	}
	
	@Test
	@ModifiedCollections(collections = {"sdRawData"})
	@InputCollectionsPath(paths = {"RawDataServiceTest/sdRawData.json"})
	public void testRawReProcessingHavingFeedVsDbRawDataMergeStrategy(){
		String dataSource="rdsp";
		String fileType = "CONSOLIDATED_INS";
		String rawDataLevel="INS";
		String rawDataId = "604f05bab2263722e3b75112";
		record.getRecordRawData().setRawAttribute(SdRawDataConstant.RAW_DATA_ID, rawDataId);
		List<RawRecordContextDetailsPojo> pojoList = getListOfPojo(record, dataSource, null, sourceUniqueIdAttributeName, feedExecutionDetailId, rawDataLevel,fileType,null,sourceUniqueIdValue);
		_id=rawDataService.handleRawData(pojoList, null,new RawDataFullLoadFilter(),null, "DefaultFeedVsDbRawdataMergeService",null);
		Assert.assertEquals(rawDataId, _id.get(0).getRecordRawData().getRawDataId());
	}
	
	@Test
	 public void testRawDataFeedContainerUpdateStrategy() {
	 String dataSource = "rdso";
	 String fileType = "GOVCORP";
	 String rawDataLevel="INS";
	 String sourceUniqueAttributeValue = "0x00102caebb6f1553";
	 List<RawRecordContextDetailsPojo> pojoList = getListOfPojo(record, dataSource, null, null, null, rawDataLevel, fileType, sourceUniqueAttributeValue, null);
	 _id=rawDataService.handleRawData(pojoList, null,new RawDataFullLoadFilter(),null, "DsosGovCorpFeedVsDbRawdataMergeService",null);
	 assertNotNull(_id.get(0).getRecordRawData());
	 }

}
