/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ObjectIdGeneratorTest.java
 * Author:	Jay Sangoi
 * Date:	25-Apr-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.id.generator;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.id.generator.ObjectIdGenerator;

/**
 * @author Jay Sangoi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class ObjectIdGeneratorTest {
	
	@Autowired
	private ObjectIdGenerator<Serializable> idGenerator;
	
	@Test
	public void test_Generate_id(){
		Assert.assertNotNull(idGenerator.generateUniqueId());
	}
}
