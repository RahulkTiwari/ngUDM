/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: UpdateDomainContainerTest.java
 * Author : SaJadhav
 * Date : 21-Aug-2019
 * 
 */
package com.smartstreamrdu.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.events.DomainContainerChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.UpdateDomainContainer;
import com.smartstreamrdu.service.listener.ListenerEvent;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class UpdateDomainContainerTest {
	@Autowired
	private UpdateDomainContainer updateDomainContainer;
	
	@Test
	public void test_isEventApplicable_true(){
		assertTrue(updateDomainContainer.isEventApplicable(ListenerEvent.DomainContainerMerged));
	}
	
	@Test
	public void test_isEventApplicable_false(){
		assertFalse(updateDomainContainer.isEventApplicable(ListenerEvent.NewDomainDataContainer));
	}
	
	@Test
	public void test_propogateEvent(){
		LocalDateTime updateTime=LocalDateTime.now();
		DataContainer dataContainer = getCountryCodeContainer(updateTime);
		dataContainer.setNew(false);
		updateDomainContainer.propogateEvent(dataContainer);
		
		assertEquals("saJadhav", dataContainer.getHighestPriorityValue(DataAttributeFactory.getAttributeByNameAndLevel("updUser", DataLevel.COUNTRY_CODES)));
		assertEquals(updateTime, dataContainer.getHighestPriorityValue(DataAttributeFactory.getAttributeByNameAndLevel("updDate", DataLevel.COUNTRY_CODES)));
	}
	
	@Test
	public void test_createInput(){

		DataContainer container = getCountryCodeContainer(LocalDateTime.now());
		DomainContainerChangeEventListenerInputCreationContext context=new DomainContainerChangeEventListenerInputCreationContext();
		context.setDomainContainer(container);
		DataContainer input = updateDomainContainer.createInput(context);
		assertEquals(container, input);
	
	}
	
	private DataContainer getCountryCodeContainer(LocalDateTime updateTime) {
		DataContainer dataContainer = new DataContainer(DataLevel.COUNTRY_CODES,
				DataContainerContext.builder().withUpdateBy("saJadhav").withUpdateDateTime(updateTime).build());
		DataValue<String> countryCodeVal=new DataValue<>();
		countryCodeVal.setValue(LockLevel.RDU, "USA");
		dataContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("code", DataLevel.COUNTRY_CODES), countryCodeVal);
		return dataContainer;
	}

}
