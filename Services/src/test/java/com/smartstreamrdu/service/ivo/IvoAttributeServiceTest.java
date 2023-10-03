/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoAttributeServiceTest.java
 * Author : SaJadhav
 * Date : 05-Apr-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoInstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoSecurityAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author SaJadhav
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes={MongoConfig.class})
public class IvoAttributeServiceTest {
	
	private  final DataAttribute ISIN_ATTR = InstrumentAttrConstant.ISIN;
	private  final DataAttribute IVO_ISIN_ATTR = IvoInstrumentAttrConstant.ISIN;
	private final DataAttribute IVO_RIC_ATTR=IvoSecurityAttrConstant.RIC;
	private final DataAttribute RIC_ATTR=SecurityAttrConstant.RIC;
	
	@Autowired
	private IvoAttributeService ivoAttributeService;
	
	
	@Test
	public void test(){
		assertEquals(IVO_ISIN_ATTR, ivoAttributeService.getIvoAttributeForSdAttribute(ISIN_ATTR));
		assertEquals(IVO_RIC_ATTR, ivoAttributeService.getIvoAttributeForSdAttribute(RIC_ATTR));
		assertEquals(IVO_RIC_ATTR, ivoAttributeService.getIvoAttributeForSdAttribute(RIC_ATTR));
	}

}
