/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: NestedArrayAttributeIvoAggregationHandlerTest.java
 * Author : SaJadhav
 * Date : 20-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
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
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoLegalEntityAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.LegalEntityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class NestedArrayAttributeIvoAggregationHandlerTest {
	private static final DataAttribute LEGAL_ENTITY_NAME_ATTR = LegalEntityAttrConstant.LEGAL_ENTITY_NAME;

	private static final String FEED_LENAME = "IBM ltd.";

	private static final DataAttribute TR_ISSUER_ORG_ID_ATTR = LegalEntityAttrConstant.TR_ISSUER_ORG_ID;

	private static final String FEED_TR_ISSUER_ORG_ID = "100313105";

	private static final DataAttribute IVO_LEGAL_ENTITY_NAME_ATTR = IvoLegalEntityAttrConstant.LEGAL_ENTITY_NAME;

	private static final DataAttribute IVO_TR_ISSUER_ORG_ID_ATTR = IvoLegalEntityAttrConstant.TR_ISSUER_ORG_ID;

	private static final String RDU_LENAME = "IBM Limited";

	private static final String RDU_TR_ISSUER_ORG_ID = "100313106";

	@Autowired
	private NestedArrayAttributeIvoAggregationHandler handler;
	
	DataContainer ivoDatContainer;
	DataContainer sdDataContainer;
	
	@Before
	public void init(){
		sdDataContainer=new DataContainer(DataLevel.INS, DataContainerContext.builder().build());
		
		DataAttribute relationAttribute=DataAttributeFactory.getRelationAttributeForInsAndLe();
		DataRow sdInsToLeRelation = addRelations(sdDataContainer, relationAttribute, null, null, RelationType.Issuer);
		DataAttribute refDataAttr = DataAttributeFactory.getRelationRefDataAttribute(relationAttribute);
		DataRow refData=new DataRow(refDataAttr);
		DataValue<String> legalEntityNameVal=new DataValue<>();
		legalEntityNameVal.setValue(LockLevel.FEED, FEED_LENAME);
		refData.addAttribute(LEGAL_ENTITY_NAME_ATTR, legalEntityNameVal);
		
		DataValue<String> trIssuerOrgIdVal=new DataValue<>();
		trIssuerOrgIdVal.setValue(LockLevel.FEED, FEED_TR_ISSUER_ORG_ID);
		refData.addAttribute(TR_ISSUER_ORG_ID_ATTR, trIssuerOrgIdVal);
		sdInsToLeRelation.addAttribute(refDataAttr, refData);
		
		ivoDatContainer=new DataContainer(DataLevel.IVO_INS, DataContainerContext.builder().build());
		DataAttribute ivoRelationAttribute=DataAttributeFactory.getIvoRelationAttributeForInsAndLe();
		DataRow ivoInsToLeRelation = addRelations(ivoDatContainer, ivoRelationAttribute, null, null, RelationType.Issuer);
		
		DataAttribute ivoRefDataAttr = DataAttributeFactory.getRelationRefDataAttribute(ivoRelationAttribute);
		DataRow ivoRefData=new DataRow(ivoRefDataAttr);
		DataValue<String> ivoLegalEntityNameVal=new DataValue<>();
		ivoLegalEntityNameVal.setValue(LockLevel.RDU, RDU_LENAME);
		ivoRefData.addAttribute(IVO_LEGAL_ENTITY_NAME_ATTR, ivoLegalEntityNameVal);
		
		DataValue<String> ivoTrIssuerOrgIdVal=new DataValue<>();
		ivoTrIssuerOrgIdVal.setValue(LockLevel.RDU, RDU_TR_ISSUER_ORG_ID);
		ivoRefData.addAttribute(IVO_TR_ISSUER_ORG_ID_ATTR, ivoTrIssuerOrgIdVal);
		ivoInsToLeRelation.addAttribute(ivoRefDataAttr, ivoRefData);
	}
	
	private DataRow addRelations(DataContainer dataContainer, DataAttribute relationAttribute,String objectId,String documentId,RelationType relationType) {
		DataRow ivoRelLink = new DataRow(relationAttribute);
		DataAttribute relationTypeAttr=DataAttributeFactory.getRelationTypeAttribute(relationAttribute);
		
		DataValue<String> relationTypeVal=new DataValue<>();
		relationTypeVal.setValue(LockLevel.RDU, relationType.name());
		ivoRelLink.addAttribute(relationTypeAttr, relationTypeVal);
		
		if(objectId!=null && documentId!=null){
			DataAttribute referenceIdAttr=DataAttributeFactory.getRelationRefIdAttribute(relationAttribute);
			DataValue<ReferenceId> referenceIdVal=new DataValue<>();
			referenceIdVal.setValue(LockLevel.RDU, new ReferenceId(objectId, documentId));
			ivoRelLink.addAttribute(referenceIdAttr, referenceIdVal);
		}
		ArrayList<DataRow> relLinkList = new ArrayList<>();
		relLinkList.add(ivoRelLink);
		
		DataValue<ArrayList<DataRow>> relDataValue=new DataValue<>();
		relDataValue.setValue(LockLevel.RDU, relLinkList);
		DataRow relDataRow=new DataRow(relationAttribute,relDataValue);
		dataContainer.addAttributeValue(relationAttribute, relDataRow);
		return ivoRelLink;
	}
	
	@Test
	public void test_handleMerge(){
		
		DataAttribute ivoInsLeRelAttr=DataAttributeFactory.getIvoRelationAttributeForInsAndLe();
		handler.handleAttributeMerge(sdDataContainer, ivoDatContainer, ivoInsLeRelAttr);
		
		DataAttribute insLeRelAttr = DataAttributeFactory.getRelationAttributeForInsAndLe();
		DataRowIterator rowIterator=new DataRowIterator(sdDataContainer,insLeRelAttr);
		DataRow dataRow = rowIterator.next();
		assertEquals(RelationType.Issuer.name(), dataRow.getAttributeValueAtLevel(LockLevel.RDU,DataAttributeFactory.getRelationTypeAttribute(insLeRelAttr)));
		
		DataRow refDataVal=(DataRow) dataRow.getAttributeValue(DataAttributeFactory.getRelationRefDataAttribute(insLeRelAttr));
		
		assertEquals(RDU_LENAME,refDataVal.getAttributeValueAtLevel(LockLevel.RDU, LEGAL_ENTITY_NAME_ATTR));
		
		
	}
}
