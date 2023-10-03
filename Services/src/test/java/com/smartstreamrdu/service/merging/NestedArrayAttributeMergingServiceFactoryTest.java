package com.smartstreamrdu.service.merging;

import org.junit.Assert;
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
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MergingTestConfiguration.class })
public class NestedArrayAttributeMergingServiceFactoryTest extends AbstractEmbeddedMongodbJunitParent {

	@Autowired
	private NestedArrayAttributeMergingServiceFactory factory;

	@Test
	public void testGetMergingService() {

		DataContainer dc = new DataContainer(DataLevel.INS, DataContainerContext.builder().build());

		DomainType dt = new DomainType();
		dt.setVal("trdse");

		DataValue<DomainType> dataValue = new DataValue<>();
		dataValue.setValue(LockLevel.FEED, dt);

		dc.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), dataValue);

		NestedArrayAttributeMergingService service = factory.getMergingService(dc);

		Assert.assertEquals(DefaultNestedArrayAttributeMergingService.class, service.getClass());

		dt.setVal("snpXfr");

		service = factory.getMergingService(dc);

		Assert.assertEquals(FeedDataOverrideNestedArrayAttributeMergingService.class, service.getClass());
	}

	@Test
	public void testGetMergingService2() {

		DataContainer dc = new DataContainer(DataLevel.INS,
				DataContainerContext.builder().withProgram("NG-EquityWeb").build());

		DomainType dt = new DomainType();
		dt.setVal("rduEns");

		DataValue<DomainType> dataValue = new DataValue<>();
		dataValue.setValue(LockLevel.FEED, dt);

		dc.addAttributeValue(DataAttributeFactory.getDatasourceAttribute(DataLevel.INS), dataValue);

		NestedArrayAttributeMergingService service = factory.getMergingService(dc);

		Assert.assertEquals(DefaultNestedArrayAttributeMergingService.class, service.getClass());

	}
}
