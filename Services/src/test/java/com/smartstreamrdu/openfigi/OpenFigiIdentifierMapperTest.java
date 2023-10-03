/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	OpenFigiIdentifierMapperTest.java
 * Author:	Shruti Arora
 * Date:	08-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.openfigi;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.openfigi.OpenFigiIdentifierMapper;
import com.smartstreamrdu.util.Constant.OpenFigiContants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class OpenFigiIdentifierMapperTest {

	@Autowired
	OpenFigiIdentifierMapper mapper;
	
	@Test
	public void mapperTest() {
		
		VfsFigiRequestMessage figiIdentifiers= null;
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.ISIN));
		
		figiIdentifiers= new VfsFigiRequestMessage();
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.ISIN));
		
		mapper.setFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.ISIN, "isin2");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.SEDOL, "sedol2");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.CUSIP, "cusip2");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.EX_CODE, "XCME1");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.TRADE_CURR, "USD1");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.ISIN, "isin3");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.SEDOL, "sedol3");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.CUSIP, "cusip3");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.EX_CODE, "XCME3");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.TRADE_CURR, "USD3");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, "xyz", "XCME1");
		mapper.setFigiIdentifierValueByName(null, OpenFigiContants.ISIN, "isin3");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, "", "XCME1");
		mapper.setFigiIdentifierValueByName(figiIdentifiers, null, "XCME1");
		
		
		Assert.assertEquals("isin2",figiIdentifiers.getIsin());
		Assert.assertEquals("cusip2",figiIdentifiers.getCusip());
		Assert.assertEquals("sedol2",figiIdentifiers.getSedol());
		Assert.assertEquals("XCME1",figiIdentifiers.getExchangeCode());
		Assert.assertEquals("USD1",figiIdentifiers.getTradeCurrencyCode());
		
		
		mapper.setFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.EX_CODE, null);
		Assert.assertNull(figiIdentifiers.getExchangeCode());
		
		
		figiIdentifiers= getNewIdentifiers();
		Assert.assertEquals("isin1",mapper.getFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.ISIN));
		Assert.assertEquals("sedol1",mapper.getFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.SEDOL));
		Assert.assertEquals("cusip1",mapper.getFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.CUSIP));
		Assert.assertEquals("XCME",mapper.getFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.EX_CODE));
		Assert.assertEquals("USD",mapper.getFigiIdentifierValueByName(figiIdentifiers, OpenFigiContants.TRADE_CURR));
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.ISIN));
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.SEDOL));
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.CUSIP));
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.EX_CODE));
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers, "\0"+OpenFigiContants.TRADE_CURR));
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers,"xyz"));
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers,""));
		Assert.assertNull(mapper.getFigiIdentifierValueByName(figiIdentifiers,null));
		
	}
	
	private VfsFigiRequestMessage getNewIdentifiers() {
		VfsFigiRequestMessage fi= new VfsFigiRequestMessage();
		fi.setIsin("isin1");
		fi.setCusip("cusip1");
		fi.setSedol("sedol1");
		fi.setExchangeCode("XCME");
		fi.setTradeCurrencyCode("USD");
		fi.setDependentMics(new ArrayList<>());
		fi.setMicIndex(-1);
		return fi;
	}
}
