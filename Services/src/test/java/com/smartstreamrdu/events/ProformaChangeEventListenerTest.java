package com.smartstreamrdu.events;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.mock.config.ProducerFactoryMockConfig;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.FeedConfigMessagePropagationType;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProducerFactoryMockConfig.class })
public class ProformaChangeEventListenerTest {
	
	private DataContainer dataContainer = null;
	
	@Autowired
	private ProformaChangeEventListener listener;
	
	@Autowired
	private ProducerFactory producerFactory;
	
	
	private Message message;

	
	private static final DataAttribute dataSourceAttribute = DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document);
	private static final DataAttribute updateDateAttribute = DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.updateDate, DataLevel.Document);
	private static final DataAttribute isinAttribute = DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.INS);
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void init(){
		
		dataContainer = new DataContainer(DataLevel.INS, null);
		dataContainer.set_id("5d5ead25fd03c713883bdedd");
		
		DataValue<DomainType> dataSourceValue = new DataValue<>();
		DomainType domainValue = new DomainType();
		domainValue.setVal("esmaFirds");
		
		dataSourceValue.setValue(LockLevel.RDU, domainValue);
		dataContainer.addAttributeValue(dataSourceAttribute, dataSourceValue);
		
		DataValue<LocalDateTime> updateDateValue = new DataValue<>();
		updateDateValue.setValue(LockLevel.RDU, LocalDateTime.of(2019, 8, 14, 12, 30));
		dataContainer.addAttributeValue(updateDateAttribute, updateDateValue);
		
		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.RDU, "XS0129477047");
		dataContainer.addAttributeValue(isinAttribute, isinValue);
		message=null;
		Mockito.when(producerFactory.getProducer(ProducerEnum.Kafka)).thenReturn(new Producer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Future sendMessage(Message msg) throws Exception {
				message = msg;
				return null;
			}

			@Override
			public Object sendMessageSync(Message msg) throws Exception {
				return msg;
			}

		});
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
	public void test_propogateEvent(){
		UpdateChangeEventListenerInputCreationContext input = new UpdateChangeEventListenerInputCreationContext();
		input.setDbDataContainer(dataContainer);
		FeedConfiguration feedConfiguration = new FeedConfiguration();
		input.setFeedConfiguration(feedConfiguration);
		listener.propogateEvent(listener.createInput(input));
	}

	@Test
	public void test_propogateEventWithRawMessagePropagationType() throws Exception{
		ChangeEventInputPojo input = new ChangeEventInputPojo();
		dataContainer.setNew(false);
		dataContainer.setHasChanged(false);
		input.setPostChangeContainer(dataContainer);
		FeedConfiguration feedConfiguration = new FeedConfiguration();
		feedConfiguration.setMessagePropagationType(FeedConfigMessagePropagationType.RAW);
		input.setFeedConfiguration(feedConfiguration);
		assertTrue((boolean) Whitebox.invokeMethod(listener, "shouldSendEventToProforma", input));
	}
	
	@Test
	public void test_propogateEventwithNONE(){
		UpdateChangeEventListenerInputCreationContext input = new UpdateChangeEventListenerInputCreationContext();
		dataContainer.setNew(false);
		dataContainer.setHasChanged(false);
		input.setDbDataContainer(dataContainer);
		FeedConfiguration feedConfiguration = new FeedConfiguration();
		feedConfiguration.setMessagePropagationType(FeedConfigMessagePropagationType.NONE);
		input.setFeedConfiguration(feedConfiguration);
		listener.propogateEvent(listener.createInput(input));
		assertNull(message);
		dataContainer.setNew(true);
		dataContainer.setHasChanged(true);

	}
}
