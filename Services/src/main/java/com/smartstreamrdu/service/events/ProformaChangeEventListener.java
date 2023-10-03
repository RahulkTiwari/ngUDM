package com.smartstreamrdu.service.events;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.proforma.ProformaMessage;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.xrf.messaging.ProformaMessageGenerator;
import com.smartstreamrdu.util.Constant.Process;
import com.smartstreamrdu.util.FeedConfigMessagePropagationType;

@Component
public class ProformaChangeEventListener implements EventListener<ChangeEventInputPojo> {

	private static final Logger _logger = LoggerFactory.getLogger(ProformaChangeEventListener.class);

	@Autowired
	private ProformaMessageGenerator proformaMessageGenerator;

	@Autowired
	private ProducerFactory producerFactory;

	@Autowired
	private XrfEligibilityEvaluator xrfEligibilityEvaluator;
	
	@Override
	public void propogateEvent(ChangeEventInputPojo changeEventPojo) {
		try {
			if (changeEventPojo == null || changeEventPojo.getPostChangeContainer() == null) {
				_logger.error("Cannot send message to DIS Proforma as the change event input is null{}", "");
				throw new IllegalStateException(
						"Cannot send message to DIS Proforma as the change event input is null");
			}

			DataContainer dataContainer = changeEventPojo.getPostChangeContainer();
			List<ProformaMessage> proformaMessages = null;

			// If data container is eligible for xrf i.e. when isDataContainerEligibleForXrf returns true 
			// then we are not going to send message from UDL to proforma.
			// We will send message from UDL to XRF and XRF will then send message to proforma
			if (dataContainer.getLevel().equals((DataLevel.IVO_INS))
					|| xrfEligibilityEvaluator.isDataContainerEligibleForXrf(dataContainer)) {
				_logger.info(
						"Not sending message to DIS Proforma as data level is SdIvo or DataSource is not eligible for sending message to Proforma for datalevel {} and _id {}",
						dataContainer.getLevel(), dataContainer.get_id());
				return;
			}

			// Need to handle the proforma message generation when the container level is
			// IVO_INS (Instrument level change at UI)
			// Generate the proforma message.
			if( shouldSendEventToProforma(changeEventPojo)) {
				proformaMessages = proformaMessageGenerator.generateMessage(changeEventPojo);
			}
			if (CollectionUtils.isEmpty(proformaMessages)) {
				_logger.info(
						"Not sending message to DIS Proforma as the ProformaMessage generated for changeEventPojo {} was : {}",
						changeEventPojo, proformaMessages);
				return;
			}

			for (ProformaMessage proformaMessage : proformaMessages) {
				createAndsendMessageToProformaQueue(proformaMessage);
			}

		} catch (Exception e) {
			_logger.error("Following error occured while sending messagee to Proforma", e);
		}
	}

	
	/**
	 * Check whether a dataContainer is eligible to send a event to proforma.
	 * 
	 * Returns true when
	 * </p>
	 *  (dataContainer level is INS AND dataSource is eligible for proforma<br>
	 * dataContainer is new
	 * 
	 * <p>
	 * RAW.equals(feedConfiguration.getMessagePropagationType() -> this check is added to make sure that update on raw fields also trigger proforma event.
	 *
	 * @param postChangeDataContainer
	 * @return 
	 */
	private boolean shouldSendEventToProforma(ChangeEventInputPojo changeEventPojo) {
		DataContainer dataContainer = changeEventPojo.getPostChangeContainer();
		List<DataContainer> childDataContainers = dataContainer.getChildDataContainers(DataLevel.SEC);
		DataContainerContext dataContainerContext = dataContainer.getDataContainerContext();
		return (dataContainer.isNew() ||(dataContainerContext!=null && dataContainerContext.isReprocessingFromUI())||
				dataContainer.hasContainerChanged() || (childDataContainers !=null && childDataContainers.stream().anyMatch(DataContainer::hasContainerChanged))
				|| FeedConfigMessagePropagationType.RAW==changeEventPojo.getFeedConfiguration().getMessagePropagationType());
	}
	
	private void createAndsendMessageToProformaQueue(ProformaMessage proformaMessage) {
		// Build the kafka message.
		Message input = new DefaultMessage.Builder().data(proformaMessage).process(Process.FileLoad).target(com.smartstreamrdu.util.Constant.Component.PROFORMA_ENGINE).build();

		Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);

		try {
			producer.sendMessage(input);
			_logger.debug("Message sent to DIS Proforma with details: {}",input);
		} catch (Exception e) {
			_logger.error("Following error occured while sending message to DIS Proforma", e);
		}
		
	}

	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return ListenerEvent.DataUpdate == event;
	}

	@Override
	public ChangeEventInputPojo createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		
		if (!(inputCreationContext instanceof UpdateChangeEventListenerInputCreationContext)) {
			_logger.error("{} requires a input creation context of type UpdateChangeEventListenerInputCreationContext. Supplied input creation context was of type {}", this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException("Argument of incorrect type supplied input creation is not supported for "+this.getClass().getName());
		}
		
		UpdateChangeEventListenerInputCreationContext inputContext = (UpdateChangeEventListenerInputCreationContext) inputCreationContext;
		
		ChangeEventInputPojo changeEventInput = new ChangeEventInputPojo();
		changeEventInput.setPreChangeContainer(inputContext.getFeedDataContaier());
		changeEventInput.setPostChangeContainer(inputContext.getDbDataContainer());
		changeEventInput.setFeedConfiguration(inputContext.getFeedConfiguration());
		return changeEventInput;
	}

}