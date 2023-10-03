package com.smartstreamrdu.service.openfigi;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@ActiveProfiles("EmbeddedMongoTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class VfsOpenFigiServicesUtilTest extends AbstractEmbeddedMongodbJunitParent{

	@Autowired
	private VfsOpenFigiServicesUtil figiServicesUtil;
	
	@Test
	public void getFigiRequestParameterMaturityDayTest() {
		long maturityDays = figiServicesUtil.getFigiRequestParameterMaturityDay();
		assertEquals(7L, maturityDays);
		
	}

}
