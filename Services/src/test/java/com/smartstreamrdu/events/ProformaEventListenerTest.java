package com.smartstreamrdu.events;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.service.events.ProformaChangeEventListener;
import com.smartstreamrdu.service.events.UpdateChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.FeedConfigMessagePropagationType;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class ProformaEventListenerTest {

	private DataContainer dataContainer = null;

	@Autowired
	private ProformaChangeEventListener listener;

	private static final DataAttribute dataSourceAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
	private static final DataAttribute updateDateAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel(Constant.ListenerConstants.updateDate, DataLevel.Document);
	private static final DataAttribute isinAttribute = DataAttributeFactory.getAttributeByNameAndLevel("isin",
			DataLevel.INS);

	@Before
	public void init() {

		dataContainer = new DataContainer(DataLevel.INS, null);

		DataValue<DomainType> dataSourceValue = new DataValue<>();
		DomainType domainValue = new DomainType();
		domainValue.setVal("asbIsin");

		dataSourceValue.setValue(LockLevel.RDU, domainValue);
		dataContainer.addAttributeValue(dataSourceAttribute, dataSourceValue);

		DataValue<LocalDateTime> updateDateValue = new DataValue<>();
		updateDateValue.setValue(LockLevel.RDU, LocalDateTime.of(2022, 8, 14, 12, 30));
		dataContainer.addAttributeValue(updateDateAttribute, updateDateValue);

		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "ABC");
		dataContainer.addAttributeValue(isinAttribute, isinValue);

	}

	@Test
	public void testIsEventApplicable_positive() {
		Assert.assertTrue(listener.isEventApplicable(ListenerEvent.DataUpdate));
	}

	@Test
	public void testIsEventApplicable_negetive() {
		Assert.assertTrue(!listener.isEventApplicable(ListenerEvent.MergeComplete));
	}

	@Test
	public void testCreateInput() {
		UpdateChangeEventListenerInputCreationContext input = new UpdateChangeEventListenerInputCreationContext();
		input.setDbDataContainer(dataContainer);
		ChangeEventInputPojo inputPojo = listener.createInput(input);
		assertNotNull(inputPojo);
		assertNotNull(inputPojo.getPostChangeContainer());
		assertNull(inputPojo.getFeedConfiguration());

	}

	@Test
	public void test_propogateEventwithRAW() throws Exception {
		ChangeEventInputPojo input = new ChangeEventInputPojo();
		dataContainer.setNew(false);
		dataContainer.setHasChanged(false);
		input.setPostChangeContainer(dataContainer);
		FeedConfiguration feedConfiguration = new FeedConfiguration();
		feedConfiguration.setMessagePropagationType(FeedConfigMessagePropagationType.RAW);
		input.setFeedConfiguration(feedConfiguration);
		assertTrue((boolean) Whitebox.invokeMethod(listener, "shouldSendEventToProforma", input));

	}
}
