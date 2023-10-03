/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ISINValidatorTest.java
 * Author:	S Padgaonkar
 * Date:	06-Sept-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.validators;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;

public class ISINValidatorTest {

	@Test
	public void ISIN_Test() {
		IsinValidation isinValidator = IsinValidation.getInstance(false);
		Assert.assertEquals(true, isinValidator.isValid("AE009A2F5S55"));
		Assert.assertEquals(false, isinValidator.isValid("BBG00BL141G5"));

	}

}
