/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: XrfChangeEventListener.java
 * Author: Rushikesh Dedhia
 * Date: May 11, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.events;



import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.xrf.XrfMessage;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.XrfMessagePropogation;
import com.smartstreamrdu.domain.message.UdmMessageKey;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.message.XrfUdmMessageKeyGenerationService;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.xrf.messaging.XrfMessageGenerator;
import com.smartstreamrdu.util.Constant.Process;
import com.smartstreamrdu.util.EventListenerConstants;
import com.smartstreamrdu.util.FeedConfigMessagePropagationType;
import com.smartstreamrdu.util.UdmSystemPropertiesConstant;

/**
 * @author Dedhia
 *
 */
@Component
public class XrfChangeEventListener implements EventListener<ChangeEventInputPojo> {
	
	
	private static final Logger _logger = LoggerFactory.getLogger(XrfChangeEventListener.class);

	@Autowired
	private XrfMessageGenerator xrfMessageGenerator;
	
	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	private XrfEligibilityEvaluator xrfEligibilityEvaluator;
	
	@Autowired
	private XrfUdmMessageKeyGenerationService xrfUdmMessageKeyGenerationService;
	
	@Autowired
	private UdmSystemPropertiesCache systemPropertiesCache;
	
	
	/* 
	 * This method converts the ChangeEventInputPojo into the the XrfJsonMessage and sends it to the XRF queue.
	 * @see com.smartstreamrdu.service.events.ChangeEventListener#propogateChangeEvent(com.smartstreamrdu.service.events.ChangeEventInputPojo)
	 */
	@Override
	public void propogateEvent(ChangeEventInputPojo changeEventPojo) {

		if (changeEventPojo != null && changeEventPojo.getPostChangeContainer() != null) {

			DataContainer dataContainer = changeEventPojo.getPostChangeContainer();
			try {
				//Don't send message for LE and if xrfSourcePriority is negative
				//In future when we support XRF on LE, we need to remove First condition
				
				if (!isDataContainerEligibleToSendForXrf(dataContainer,changeEventPojo.getFeedConfiguration())) {
					_logger.debug(
							"Not sending message to XRF for datalevel {} and dataContainer id {} since it is not eligible",
							dataContainer.getLevel(), dataContainer.get_id());
					return;
				}
				
				XrfMessage xrfMessage = xrfMessageGenerator.generateMessage(changeEventPojo,null); 

				if (null == xrfMessage) {
					_logger.info("Not sending message to XRF as the XrfMessage generated for changeEventPojo {} was : {}", changeEventPojo, xrfMessage);
					return;
				}
				
				UdmMessageKey udmMessageKey = xrfUdmMessageKeyGenerationService.generateUdmMessageKey(xrfMessage);
				
				if (null == udmMessageKey) {
					_logger.info("Not sending message to XRF as the udmMessageKey for xrfMessage : {} is null ", xrfMessage);
					return;
				}

				Message input = new DefaultMessage.Builder().data(xrfMessage).key(udmMessageKey).process(Process.FileLoad).target(com.smartstreamrdu.util.Constant.Component.XRF).build();

				Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);


				producer.sendMessage(input);
				_logger.debug("Message sent to XRF with details: {}",input);
			} catch (Exception e) {
				_logger.error("Following error occured while sending messagee to XRF", e);
			}
		} else {
			_logger.error("Cannot send message to XRF as the change event input is null{}","");
			throw new IllegalStateException("Cannot send message to XRF as the change event input is null");
		}

	}
	
	/**
	 * Check whether a dataContainer is eligible to send a event to XRF.
	 * 
	 * Returns true when
	 * </p>
	 *  (dataContainer level is INS AND dataSource is eligible for XRF) AND
	 *</p>
	 * updateProgramme is UUI OR
	 * </p> 
	 * xrfMessagePropogation  is ALWAYS	OR
	 * </p>
	 * xrfMessagePropogation is ANY_UPDATE AND dataContainer.hasChanged = true OR
	 * </p>
	 * dataContainer is new
	 * 
	 * <p>
	 * RAW.equals(feedConfiguration.getMessagePropagationType() -> this check is added to make sure that update on raw fields also trigger xrf event.
	 * 
	 * @param dataContainer
	 * @param feedConfiguration
	 * @return
	 * @throws UdmBaseException 
	 */
	private boolean isDataContainerEligibleToSendForXrf(DataContainer dataContainer, FeedConfiguration feedConfiguration) throws UdmBaseException {
		String updateProgram = dataContainer.getDataContainerContext().getProgram();

		Optional<String> xrfMessagePropogationProperty = systemPropertiesCache.getPropertiesValue(
				UdmSystemPropertiesConstant.XRF_MESSAGE_PROPOGATION, DataLevel.UDM_SYSTEM_PROPERTIES);
		String xrfMessagePropogation = xrfMessagePropogationProperty.orElse(XrfMessagePropogation.ANY_UPDATE.name());
		
		return (DataLevel.INS.equals(dataContainer.getLevel())
				&& xrfEligibilityEvaluator.isDataContainerEligibleForXrf(dataContainer))
				&& (dataContainer.isNew() || EventListenerConstants.PROGRAME_UUI.equals(updateProgram)
						|| XrfMessagePropogation.ALWAYS.name().equals(xrfMessagePropogation)
						|| hasDataContainerChanged(xrfMessagePropogation, dataContainer)
						|| FeedConfigMessagePropagationType.RAW==(feedConfiguration!=null?feedConfiguration.getMessagePropagationType():null));
	}

	/**
	 * Returns true if xrfMessagePropogation is ANY_UPDATE and  DataContainer is changed at INS or SEC level
	 * @param xrfMessagePropogation 
	 * 
	 * @param dataContainer
	 * @return
	 */
	private boolean hasDataContainerChanged(String xrfMessagePropogation, DataContainer dataContainer) {

		return XrfMessagePropogation.ANY_UPDATE.name().equals(xrfMessagePropogation)
				&& (dataContainer.hasContainerChanged() || dataContainer.getChildDataContainers(DataLevel.SEC).stream()
						.anyMatch(DataContainer::hasContainerChanged));
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#isEventApplicable(com.smartstreamrdu.service.listener.ListenerEvent)
	 */
	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return ListenerEvent.DataUpdate == event;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#createInput(java.lang.Object[])
	 */
	@Override
	public ChangeEventInputPojo createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		
		if (!(inputCreationContext instanceof UpdateChangeEventListenerInputCreationContext)) {
			_logger.error("{} requires a input creation context of type UpdateChangeEventListenerInputCreationContext. Supplied input creation context was of type {}", this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException("Argument of incorrect type supplied input creation is not supported for "+this.getClass().getName());
		}
		
		UpdateChangeEventListenerInputCreationContext inputContext = (UpdateChangeEventListenerInputCreationContext) inputCreationContext;
		
		ChangeEventInputPojo inp = new ChangeEventInputPojo();
		inp.setPreChangeContainer(inputContext.getFeedDataContaier());
		inp.setPostChangeContainer(inputContext.getDbDataContainer());
		inp.setFeedConfiguration(inputContext.getFeedConfiguration());
		if (inputContext.getXrfProcessMode() != null) {
			inp.addToMessageContext(EventListenerConstants.XRF_PROCESS_MODE, inputContext.getXrfProcessMode());
		}
		return inp;
	}

}