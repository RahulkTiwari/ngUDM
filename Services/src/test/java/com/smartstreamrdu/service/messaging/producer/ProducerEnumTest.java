/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ProducerEnumTest.java
 * Author:	Jay Sangoi
 * Date:	23-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.messaging.producer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jay Sangoi
 *
 */
public class ProducerEnumTest {

	@Test
	public void test_COnstant() {
		Assert.assertNotNull(ProducerEnum.Kafka.name());
	}

}
