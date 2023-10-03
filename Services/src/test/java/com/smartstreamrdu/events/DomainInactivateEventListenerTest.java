/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainInactivateEventListenerTest.java
 * Author : SaJadhav
 * Date : 21-Aug-2019
 * 
 */
package com.smartstreamrdu.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.service.events.DomainContainerChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.DomainInactivateEventListener;
import com.smartstreamrdu.service.listener.ListenerEvent;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DomainInactivateEventListenerTest extends AbstractEmbeddedMongodbJunitParent{
	
	private final static DataAttribute INS_Vendor_mapping = DataAttributeFactory
			.getAttributeByNameAndLevel("vendorMappings", DataLevel.DV_DOMAIN_MAP);
	private final static DataAttribute NORMALIZED_VALUE=DataAttributeFactory.getAttributeByNameAndLevel("normalizedValue", DataLevel.DV_DOMAIN_MAP);
	private final static DataAttribute RDU_DOMAIN=DataAttributeFactory.getAttributeByNameAndLevel("rduDomain", DataLevel.DV_DOMAIN_MAP);
	private final static DataAttribute DOMAIN_STATUS=DataAttributeFactory.getAttributeByNameAndLevelAndParent("status", DataLevel.DV_DOMAIN_MAP,INS_Vendor_mapping);
	
	@Autowired
	private DomainInactivateEventListener inactivateEventListener;
	
	@Autowired
	private DataRetrievalService retrievalService;
	
	@Test
	public void testIsEventApplicable_true(){
		assertTrue(inactivateEventListener.isEventApplicable(ListenerEvent.DomainContainerUpdate));
	}
	@Test
	public void testIsEventApplicable_false(){
		assertFalse(inactivateEventListener.isEventApplicable(ListenerEvent.NewDataContainer));
	}
	
	@Test
	@InputCollectionsPath(paths={"DomainInactivateEventListenerTest"})
	@ModifiedCollections(collections={"dvDomainMap"})
	public void testPropogateEvent() throws UdmTechnicalException{
		//create pre requisite data
		LocalDateTime updDate=LocalDateTime.now();
		DataContainer currencyContainer = getUAECurrencyContainer("saJadhav",updDate);
		currencyContainer.setNew(false);
		inactivateEventListener.propogateEvent(currencyContainer);
		//verify output
		DataContainer domainMapContainer = findDomainMapBasedonRduDomainAndNormalizedVal("currencyCodes","AED");
		assertEquals("AED", domainMapContainer.getHighestPriorityValue(NORMALIZED_VALUE));
		assertEquals("currencyCodes", domainMapContainer.getHighestPriorityValue(RDU_DOMAIN));
		assertEquals("saJadhav", domainMapContainer.getHighestPriorityValue(DataAttributeFactory.getAttributeByNameAndLevel("updUser", DataLevel.DV_DOMAIN_MAP)));
		DataRowIterator dataRowIterator=new DataRowIterator(domainMapContainer, INS_Vendor_mapping);
		while (dataRowIterator.hasNext()) {
			DataRow dataRow = dataRowIterator.next();
			assertEquals("I", dataRow.getHighestPriorityAttributeValue(DOMAIN_STATUS));
		}
	}
	
	@Test
	public void testCreateInput(){
		LocalDateTime updDate=LocalDateTime.now();
		DataContainer currencyContainer = getUAECurrencyContainer("saJadhav",updDate);
		DomainContainerChangeEventListenerInputCreationContext context=new DomainContainerChangeEventListenerInputCreationContext();
		context.setDomainContainer(currencyContainer);
		DataContainer input = inactivateEventListener.createInput(context);
		assertEquals(currencyContainer, input);
	}
	
	private DataContainer findDomainMapBasedonRduDomainAndNormalizedVal(String rduDomain, String primaryKeyValue) throws UdmTechnicalException {
		DataRetrivalInput input = getDataRetrievalInput(rduDomain, primaryKeyValue);
		List<DataContainer> results = retrievalService.retrieve(Database.Mongodb, input);
		if(CollectionUtils.isEmpty(results)){
			return null;
		}
		return results.get(0);
	}

	private DataRetrivalInput getDataRetrievalInput(String rduDomain, String primaryKeyValue) {
		DataValue<String> rduDomainVal=new DataValue<>();
		rduDomainVal.setValue(LockLevel.RDU, rduDomain);
		Criteria rduDomainCriteria=Criteria.where(RDU_DOMAIN).is(rduDomainVal);
		
		DataValue<String> normalizedValue=new DataValue<>();
		normalizedValue.setValue(LockLevel.RDU, primaryKeyValue);
		Criteria normalizedValCriteria=Criteria.where(NORMALIZED_VALUE).is(normalizedValue);
		
		DataRetrivalInput input=new DataRetrivalInput();
		input.setCriteria(new Criteria().andOperator(rduDomainCriteria,normalizedValCriteria));
		return input;
	}
	
	private DataContainer getUAECurrencyContainer(String userName, LocalDateTime localDateTime) {
		DataAttribute code = DataAttributeFactory.getAttributeByNameAndLevel("code", DataLevel.CURRENCY_CODES);
		DataAttribute name = DataAttributeFactory.getAttributeByNameAndLevel("name", DataLevel.CURRENCY_CODES);
		DataAttribute flagActive = DataAttributeFactory.getAttributeByNameAndLevel("status", DataLevel.CURRENCY_CODES);
		DataValue<String> codeVal = new DataValue<String>();
		codeVal.setValue(LockLevel.RDU, "AED");
		DataValue<String> nameVal = new DataValue<String>();
		nameVal.setValue(LockLevel.RDU, "UAE Dirham");
		DataContainer dataContainer = new DataContainer(DataLevel.CURRENCY_CODES,
				DataContainerContext.builder().withUpdateBy(userName).withUpdateDateTime(localDateTime).build());
		dataContainer.addAttributeValue(code, codeVal);
		dataContainer.addAttributeValue(name, nameVal);
		DataValue<String> statusActiveVal = new DataValue<>();
		statusActiveVal.setValue(LockLevel.RDU, "I");
		dataContainer.addAttributeValue(flagActive, statusActiveVal);
		return dataContainer;
	}

}
