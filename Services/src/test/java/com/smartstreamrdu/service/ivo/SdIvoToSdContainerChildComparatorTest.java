/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: SdIvoToSdContainerChildComparatorTest.java
* Author : VRamani
* Date : May 15, 2019
* 
*/
package com.smartstreamrdu.service.ivo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.IvoDocType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.util.Constant.SdAttributeNames;
import com.smartstreamrdu.util.IvoConstants;

/**
* @author VRamani
*
*/
@Profile("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { IvoLockServiceConfig.class })
public class SdIvoToSdContainerChildComparatorTest extends IvoLockTestUtil {
	private static final DataAttribute IVO_DOC_TYPE_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("docType",
			DataLevel.IVO_DOC);
	private static final DataAttribute IVO_TICKER_ATTR = DataAttributeFactory.getAttributeByNameAndLevel("ticker",
			DataLevel.IVO_SEC);

	private static final String OPS_TICKER = "BIT";
	private static final DataAttribute IVO_SECURITY_RELATIONS_ATTR = DataAttributeFactory
			.getAttributeByNameAndLevel(IvoConstants.SECURITY_RELATIONS, DataLevel.IVO_SEC);
	
	@Autowired
	SdIvoToSdContainerChildComparator sdIvoToSdContainerChildComparator;
	
	/**
	 * @return
	 */
	private DataContainer createSdIvoContainer() {
		DataContainer sdIvoContainer = new DataContainer(DataLevel.IVO_INS, DataContainerContext.builder().build());
		DataValue<String> docTypeVal = new DataValue<>();
		docTypeVal.setValue(LockLevel.RDU, IvoDocType.RDU.name());
		sdIvoContainer.addAttributeValue(IVO_DOC_TYPE_ATTR, docTypeVal);

		DataAttribute sdIvoInsRelAttr = DataAttributeFactory
				.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_RELATIONS, DataLevel.IVO_INS);
		DataRow ivoRelLink = new DataRow(sdIvoInsRelAttr);
		DataAttribute relationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(sdIvoInsRelAttr);

		DataValue<String> relationTypeVal = new DataValue<>();
		relationTypeVal.setValue(LockLevel.RDU, RelationType.IVO.name());
		ivoRelLink.addAttribute(relationTypeAttr, relationTypeVal);

		DataAttribute referenceIdAttr = DataAttributeFactory.getRelationRefIdAttribute(sdIvoInsRelAttr);
		DataValue<ReferenceId> referenceIdVal = new DataValue<>();
		referenceIdVal.setValue(LockLevel.RDU, new ReferenceId(RDU_INSTRUMENT_ID, XR_DOC_ID));
		ivoRelLink.addAttribute(referenceIdAttr, referenceIdVal);

		ArrayList<DataRow> relLinkList = new ArrayList<>();
		relLinkList.add(ivoRelLink);

		DataValue<ArrayList<DataRow>> relDataValue = new DataValue<>();
		relDataValue.setValue(LockLevel.RDU, relLinkList);
		DataRow relDataRow = new DataRow(sdIvoInsRelAttr, relDataValue);
		sdIvoContainer.addAttributeValue(sdIvoInsRelAttr, relDataRow);

		DataContainer secDataContainer = DataContainerTestUtil.getDataContainer(DataLevel.IVO_SEC);
		sdIvoContainer.addDataContainer(secDataContainer, DataLevel.IVO_SEC);

		DataValue<String> tickerVal = new DataValue<>();
		tickerVal.setValue(LockLevel.RDU, OPS_TICKER);
		secDataContainer.addAttributeValue(IVO_TICKER_ATTR, tickerVal);

		DataAttribute sdIvoSecRelAttr = IVO_SECURITY_RELATIONS_ATTR;
		DataRow ivoSecRelLink = new DataRow(sdIvoSecRelAttr);
		DataAttribute secRelationTypeAttr = DataAttributeFactory.getRelationTypeAttribute(sdIvoSecRelAttr);

		DataValue<String> secRelationTypeVal = new DataValue<>();
		secRelationTypeVal.setValue(LockLevel.RDU, RelationType.IVO.name());
		ivoSecRelLink.addAttribute(secRelationTypeAttr, secRelationTypeVal);

		DataAttribute secReferenceIdAttr = DataAttributeFactory.getRelationRefIdAttribute(sdIvoSecRelAttr);
		DataValue<ReferenceId> secReferenceIdVal = new DataValue<>();
		secReferenceIdVal.setValue(LockLevel.RDU, new ReferenceId(RDU_SECURITY_ID, XR_DOC_ID));
		ivoSecRelLink.addAttribute(secReferenceIdAttr, secReferenceIdVal);

		ArrayList<DataRow> secRelLinkList = new ArrayList<>();
		secRelLinkList.add(ivoSecRelLink);

		DataValue<ArrayList<DataRow>> secRelDataValue = new DataValue<>();
		secRelDataValue.setValue(LockLevel.RDU, secRelLinkList);
		DataRow secRelDataRow = new DataRow(sdIvoSecRelAttr, secRelDataValue);
		secDataContainer.addAttributeValue(sdIvoSecRelAttr, secRelDataRow);

		return sdIvoContainer;
	}
	
	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void testValidLockScenario() {
		DataContainer sdDataContainer = createSdDataContainer();
		// first create sdIvo document
		DataContainer sdIvoContainer = createSdIvoContainer();
		DataContainer xrDataContainer = createXrDataContainer();
		List<DataContainer> sdDataContainers = new ArrayList<>();
		sdDataContainers.addAll(sdDataContainer.getAllChildDataContainers());
		
		List<DataContainer> sdIvoChildContainers = sdIvoContainer.getChildDataContainers(DataLevel.IVO_SEC);
		String sdDocumentId = sdDataContainer.get_id();
		for (DataContainer sdIvoChildContainer: sdIvoChildContainers) {
			Optional<DataContainer> sdContainer = sdIvoToSdContainerChildComparator.findMatchingContainerByIvo(sdIvoChildContainer, sdDataContainers, xrDataContainer, sdDocumentId);
			assertTrue(sdContainer.isPresent());
		}
	}
	
	@Test
	@ModifiedCollections(collections = {"sdData", "sdIvo", "xrData"})
	public void testDeletedXrSecurityScenario() {
		DataContainer sdDataContainer = createSdDataContainer();
		// first create sdIvo document
		DataContainer sdIvoContainer = createSdIvoContainer();
		DataContainer xrDataContainer = createInactiveXrDataContainer();
		List<DataContainer> sdDataContainers = new ArrayList<>();
		sdDataContainers.addAll(sdDataContainer.getAllChildDataContainers());
		
		List<DataContainer> sdIvoChildContainers = sdIvoContainer.getChildDataContainers(DataLevel.IVO_SEC);
		String sdDocumentId = sdDataContainer.get_id();
		for (DataContainer sdIvoChildContainer: sdIvoChildContainers) {
			Optional<DataContainer> sdContainer = sdIvoToSdContainerChildComparator.findMatchingContainerByIvo(sdIvoChildContainer, sdDataContainers, xrDataContainer, sdDocumentId);
			assertFalse(sdContainer.isPresent());
		}
	}

}
