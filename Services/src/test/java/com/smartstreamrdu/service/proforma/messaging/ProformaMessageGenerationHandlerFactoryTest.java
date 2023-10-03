package com.smartstreamrdu.service.proforma.messaging;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.domain.ServiceTestConfiguration;
import com.smartstreamrdu.service.xrf.messaging.DefaultProformaMessageGenerationHandler;
import com.smartstreamrdu.service.xrf.messaging.GleifProformaMessageGenerationHandler;
import com.smartstreamrdu.service.xrf.messaging.ProformaMessageGenerationHandler;
import com.smartstreamrdu.service.xrf.messaging.ProformaMessageGenerationHandlerFactory;
import com.smartstreamrdu.util.DataSourceConstants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class ProformaMessageGenerationHandlerFactoryTest {
	
	@Autowired
	ProformaMessageGenerationHandlerFactory handlerFactory;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetProformaMessageGenerationHandler_forGleif() {
		ProformaMessageGenerationHandler handler = handlerFactory.getProformaMessageGenerationHandler(DataSourceConstants.GLEIF_DS);
		Assert.assertEquals(true, handler instanceof GleifProformaMessageGenerationHandler);
	}
	
	
	@Test
	public void testGetProformaMessageGenerationHandler_forDefault() {
		ProformaMessageGenerationHandler handler = handlerFactory.getProformaMessageGenerationHandler(DataSourceConstants.REUTERS_DS);
		Assert.assertEquals(true, handler instanceof DefaultProformaMessageGenerationHandler);
	}

}
