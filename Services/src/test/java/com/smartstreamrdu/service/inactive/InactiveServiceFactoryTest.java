/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InactiveServiceFactoryTest.java
 * Author:	Jay Sangoi
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.events.InactiveBean;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.DataSourceConstants;

/**
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class InactiveServiceFactoryTest {

	@Autowired
	private InactiveServiceFactory factory;
	
	@Test
	public void test_getInactiveService(){
		InactiveBean bean = new InactiveBean();
		
		Assert.assertNull(factory.getInactiveService(bean));
		
		DataContainer container = new DataContainer(DataLevel.INS, DataContainerContext.builder().build()) ;
				
		bean.setDbContainer(container);
		bean.setDatasource(DataSourceConstants.REUTERS_DS);
		InactiveService service = factory.getInactiveService(bean);
		Assert.assertNull(service);		
		
		bean.setDatasource(DataSourceConstants.FIGI_DS);
		service = factory.getInactiveService(bean);
		Assert.assertNull(service);
		
	}
	
	@Test
	public void test_getSecurityInactiveService(){
		InactiveBean bean = new InactiveBean();
		Assert.assertNull(factory.getSecurityInactiveService(bean));
		
		DataContainer container = new DataContainer(DataLevel.INS, DataContainerContext.builder().build()) ;		
		bean.setDbContainer(container);
		bean.setDatasource("idcApex");
		InactiveService service = factory.getSecurityInactiveService(bean);
		Assert.assertNotNull(service);
		Assert.assertEquals(SecurityInActivationService.class, service.getClass());
		
		bean.setDatasource("figi");
		service = factory.getSecurityInactiveService(bean);
		Assert.assertNotNull(service);
		Assert.assertEquals(SecurityInActivationService.class, service.getClass());
		
	}
	
}
