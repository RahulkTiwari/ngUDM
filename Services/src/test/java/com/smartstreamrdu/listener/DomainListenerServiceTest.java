/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainListenerServiceTest.java
 * Author : SaJadhav
 * Date : 21-Aug-2019
 * 
 */
package com.smartstreamrdu.listener;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.listener.DomainListenerService;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DomainListenerServiceTest {
	
	@Autowired
	private DomainListenerService listenerService;
	
	@Test
	@Ignore
	public void test(){
	}
}
