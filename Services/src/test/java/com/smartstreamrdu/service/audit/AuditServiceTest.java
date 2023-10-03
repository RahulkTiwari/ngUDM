/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	AuditServiceTest.java
 * Author:	Jay Sangoi
 * Date:	19-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.audit;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.Audit;
import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.merging.DataContainerMergeException;
import com.smartstreamrdu.service.merging.DataContainerMergingService;
import com.smartstreamrdu.util.DataContainerUtil;


/**
 * @author Jay Sangoi
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class AuditServiceTest {
	
	private static String SOURCE_UNIQUE_ID_INS_VALUE_FORMAT = "%sIns%s";

	private static final String INSTRUMENT_ID = "INS123";

	private static final Object DOC_ID = "DOCID";

	private static final String TR_QUOTE_ID = "ABCHDH";

	private static final String RIC = "ABL.TR";
	
	private DataAttribute trQuoteIdAttr=DataAttributeFactory.getAttributeByNameAndLevel("trQuoteId", DataLevel.SEC);
	private DataAttribute ricAttr=DataAttributeFactory.getAttributeByNameAndLevel("ric", DataLevel.SEC);
	private DataAttribute insToInsRelationAttribute=DataAttributeFactory.getRelationAttributeForInsAndIns();
	

	@Autowired
	private AuditService auditservice;
	@Autowired
	private DataContainerMergingService mergingService;

	private DataAttribute dataSourceAttribute=DataAttributeFactory.getDatasourceAttribute(DataLevel.INS);

	
	@Test
	public void test_createAudit(){
		
		//Test case 1 - Test Exception with null data container
		try{
			DataContainer container = null;
			auditservice.createAudit(container);
			
			
		}
		catch(NullPointerException e){
			Assert.assertEquals("Data Container cannot be null for populating audit.", e.getMessage());
		}
		
		//Test case 1 - Test Exception with null data container
		try{
			List<DataContainer> cts = null;
			auditservice.createAudit(cts);
			
		}
		catch(NullPointerException e){
			Assert.assertEquals("Data Containers cannot be null for populating audit.", e.getMessage());
		}
		
		
		//Test Case 2 - New LE container
		LocalDateTime now = LocalDateTime.now();
		DataContainer leContainer = new DataContainer(DataLevel.LE, DataContainerContext.builder().withFileName("file1").withProgram("udl").withUpdateDateTime(now).build());
		
		auditservice.createAudit(leContainer);
		
		List<Audit> audits1 = leContainer.getAudit();
		
		Assert.assertNotNull(audits1);
		Assert.assertTrue(audits1.size() == 1);
		Assert.assertEquals(0, audits1.get(0).getAuditIndex());
		Assert.assertEquals("file1", audits1.get(0).getFileName());
		Assert.assertEquals("udl", audits1.get(0).getProgram());
		Assert.assertEquals(now, audits1.get(0).getUpdateDate());
		
		//Test Case 3,4 - Add New Ins and one sec container
		DataContainer insContainer = new DataContainer(DataLevel.INS, DataContainerContext.builder().withFileName("file1").withProgram("udl").withUpdateDateTime(now).build());
		DataContainer secContainer1 = new DataContainer(DataLevel.SEC, DataContainerContext.builder().withFileName("file1").withProgram("udl").withUpdateDateTime(now).build());
		insContainer.addDataContainer(secContainer1, DataLevel.SEC);
		auditservice.createAudit(insContainer);
		List<Audit>  audits2 = insContainer.getAudit();
		
		Assert.assertNotNull(audits2);
		Assert.assertTrue(audits2.size() == 1);
		Assert.assertEquals(0, audits2.get(0).getAuditIndex());
		Assert.assertEquals("file1", audits2.get(0).getFileName());
		Assert.assertEquals("udl", audits2.get(0).getProgram());
		Assert.assertEquals(now, audits2.get(0).getUpdateDate());
		List<Audit>  audits3 = insContainer.getChildDataContainers(DataLevel.SEC).get(0).getAudit();
		Assert.assertTrue(audits3.size() == 1);
		Assert.assertEquals(0, audits3.get(0).getAuditIndex());
		Assert.assertEquals("file1", audits3.get(0).getFileName());
		Assert.assertEquals("udl", audits3.get(0).getProgram());
		Assert.assertEquals(now, audits3.get(0).getUpdateDate());
		
		//Test Case 4 - Update LE Container
		leContainer.setHasChanged(true);
		auditservice.createAudit(leContainer);
		List<Audit> audits4 = leContainer.getAudit();
		
		Assert.assertNotNull(audits4);
		Assert.assertTrue(audits4.size() == 2);
		Assert.assertEquals(1, audits4.get(1).getAuditIndex());
		Assert.assertEquals("file1", audits4.get(1).getFileName());
		Assert.assertEquals("udl", audits4.get(1).getProgram());
		Assert.assertEquals(now, audits4.get(1).getUpdateDate());
		
		
		//Test Case 5 - Testinig on list
		List<DataContainer> cts = new ArrayList<>();
		DataContainer insContainer1 = new DataContainer(DataLevel.INS, DataContainerContext.builder().withFileName("file1").withProgram("udl").withUpdateDateTime(now).build());

		cts.add(insContainer1);
		auditservice.createAudit(cts);
		
		List<Audit> audits5 = cts.get(0).getAudit();
		
		Assert.assertNotNull(audits5);
		Assert.assertTrue(audits5.size() == 1);
		Assert.assertEquals(0, audits5.get(0).getAuditIndex());
		Assert.assertEquals("file1", audits5.get(0).getFileName());
		Assert.assertEquals("udl", audits5.get(0).getProgram());
		Assert.assertEquals(now, audits5.get(0).getUpdateDate());
		
	}
	
	@Test
	public void test_createAudit_Nested() throws DataContainerMergeException{
		DataContainer container = createInstrumentDataContainier();
		auditservice.createAudit(container);
		List<Audit> auditList = container.getAudit();
		assertEquals(1, auditList.size());
		
		DataContainerUtil.populateNewFlagAndHasChanged(Arrays.asList(container));
		auditservice.createAudit(container);
		auditList = container.getAudit();
		assertEquals(1, auditList.size());
		
		DataContainer containerWithRelData=getContainerWithRelationsData();
		mergingService.merge(containerWithRelData, Arrays.asList(container));		
		auditservice.createAudit(container);
		auditList = container.getAudit();
		assertEquals(2, auditList.size());
		assertEquals(0, auditList.get(0).getAuditIndex());
		assertEquals(1, auditList.get(1).getAuditIndex());
	}
	
	private DataContainer createInstrumentDataContainier() {

		DataContainerContext dataContainerContext=DataContainerContext.builder().withProgram("udl").withUpdateBy("abc.efg").withServiceDeskTicketId("UDM-98765").build();
		DataContainer insContainer = new DataContainer(DataLevel.INS, dataContainerContext);
		DataValue<String> sourceUniqueDV = new DataValue<>();
		sourceUniqueDV.setValue(LockLevel.FEED, String.format(SOURCE_UNIQUE_ID_INS_VALUE_FORMAT, LockLevel.FEED, 1));
		sourceUniqueDV.setValue(LockLevel.RDU, String.format(SOURCE_UNIQUE_ID_INS_VALUE_FORMAT, LockLevel.RDU, 1));
		insContainer.addAttributeValue(DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.INS),
				sourceUniqueDV);
		
		DataValue<DomainType> dataSourceValue=new DataValue<>();
		dataSourceValue.setValue(LockLevel.FEED, new DomainType("trdse"));
		insContainer.addAttributeValue(dataSourceAttribute, dataSourceValue);

		return insContainer;
	}
	
	/**
	 * @param retrieve
	 * @return 
	 */
	private DataContainer getContainerWithRelationsData() {
		
		DataContainer insContainer = createInstrumentDataContainier();

		DataRow relLink = new DataRow(insToInsRelationAttribute);
		DataAttribute relationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(insToInsRelationAttribute);

		DataValue<String> relationTypeVal = new DataValue<>();
		relationTypeVal.setValue(LockLevel.FEED, RelationType.Underlying.name());
		relLink.addAttribute(relationTypeAttr, relationTypeVal);

		DataAttribute referenceIdAttr = DataAttributeFactory.getRelationRefIdAttribute(insToInsRelationAttribute);
		DataValue<ReferenceId> referenceIdVal = new DataValue<>();
		referenceIdVal.setValue(LockLevel.FEED, new ReferenceId(INSTRUMENT_ID, DOC_ID));
		relLink.addAttribute(referenceIdAttr, referenceIdVal);

		DataAttribute refData = DataAttributeFactory.getRelationRefDataAttribute(insToInsRelationAttribute);
		DataRow refDataVal = new DataRow(refData);
		DataValue<String> trquoteIdVal = new DataValue<>();
		trquoteIdVal.setValue(LockLevel.FEED, TR_QUOTE_ID);
		refDataVal.addAttribute(trQuoteIdAttr, trquoteIdVal);

		DataValue<String> ricVal = new DataValue<>();
		ricVal.setValue(LockLevel.FEED, RIC);
		refDataVal.addAttribute(ricAttr, ricVal);
		relLink.addAttribute(refData, refDataVal);

		ArrayList<DataRow> relLinkList = new ArrayList<>();
		relLinkList.add(relLink);
		DataValue<ArrayList<DataRow>> relDataValue = new DataValue<>();
		relDataValue.setValue(LockLevel.FEED, relLinkList);
		DataRow relDataRow = new DataRow(insToInsRelationAttribute, relDataValue);
		insContainer.addAttributeValue(insToInsRelationAttribute, relDataRow);
		return insContainer;
	}
	
}
