/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : LegalEntityUpdateEventListener.java
 * Author :SaJadhav
 * Date : 08-Jul-2020
 */
package com.smartstreamrdu.service.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.message.LeUpdateProformaProcessingMessage;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.util.Constant.Process;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Propagates the Legal Entity updates for xrf dataSources to LeUpdateProformaProcessingQueue
 * 
 * @author SaJadhav
 *
 */
@Component
@Slf4j
public class LegalEntityUpdateEventListener implements EventListener<ChangeEventInputPojo> {
	
	private static final DataAttribute LEGAL_ENTITY_ID_ATTR = DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.LE);

	private static final DataAttribute DATASOURCE_ATTR = DataAttributeFactory.getDatasourceAttribute(DataLevel.LE);
	
	@Autowired
	@Setter
	private XrfEligibilityEvaluator xrfEligibilityEvaluator;
	
	@Autowired
	@Setter
	private ProducerFactory producerFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propogateEvent(ChangeEventInputPojo changeEventPojo) {
		if (changeEventPojo == null || changeEventPojo.getPostChangeContainer() == null) {
			log.error("Cannot send legal Entity update message to DEN as the change event input is null{}", "");
			throw new IllegalStateException(
					"Cannot send legal Entity update message to DEN as the change event input is null{}");
		}
		DataContainer postChangeContainer = changeEventPojo.getPostChangeContainer();

		try {
			if (!(DataLevel.LE.equals(postChangeContainer.getLevel())
					&& xrfEligibilityEvaluator.isDataContainerEligibleForXrf(postChangeContainer))) {
				log.info(
						"Not sending message to DEN for legal entity update since the dataContainer level is not LE or "
								+ "dataContainer is not eligible for XRF");
				return;
			}
			sendMessageToDen(postChangeContainer);
		} catch (UdmBaseException e) {
			log.error(String.format("Error occured while sending Legal entity updates to DEN for documentId %s%",
					postChangeContainer.get_id()), e);
		}

	}

	/**
	 * Sends message to topic LeUpdateProformaProcessQueue id dataContainer is modified
	 * 
	 * @param postChangeContainer
	 */
	private void sendMessageToDen(DataContainer postChangeContainer) {
		if(!postChangeContainer.isNew() && postChangeContainer.hasContainerChanged()){
			String legalEntityId = postChangeContainer.getHighestPriorityValue(LEGAL_ENTITY_ID_ATTR);
			DomainType dataSource = postChangeContainer.getHighestPriorityValue(DATASOURCE_ATTR);
			
			LeUpdateProformaProcessingMessage leProformaProcessingMessage=new
					LeUpdateProformaProcessingMessage(postChangeContainer.get_id(), legalEntityId, dataSource.getVal());
			createAndsendMessageLeProformaProcessingQueue(leProformaProcessingMessage);
			
		}
		
	}
	
	private void createAndsendMessageLeProformaProcessingQueue(LeUpdateProformaProcessingMessage leProformaProcessingMessage) {
		Message input = new DefaultMessage.Builder().data(leProformaProcessingMessage).process(Process.LE_UPDATE_PROFORMA_PROCESSING).target(com.smartstreamrdu.util.Constant.Component.DATA_ENRICHMENT).build();
		Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);
		try {
			producer.sendMessage(input);
			log.debug("Message sent to LeProformaReprocessignQueue with details: {}",leProformaProcessingMessage);
		} catch (Exception e) {
			log.error("Following error occured while sending message to LeProformaProcessignQueue ", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return ListenerEvent.DataUpdate == event;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChangeEventInputPojo createInput(ChangeEventListenerInputCreationContext inputCreationContext) {

		if (!(inputCreationContext instanceof UpdateChangeEventListenerInputCreationContext)) {
			log.error(
					"{} requires a input creation context of type UpdateChangeEventListenerInputCreationContext. Supplied input creation context was of type {}",
					this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException(
					"Argument of incorrect type supplied input creation is not supported for "
							+ this.getClass().getName());
		}

		UpdateChangeEventListenerInputCreationContext inputContext = (UpdateChangeEventListenerInputCreationContext) inputCreationContext;

		ChangeEventInputPojo changeEventInput = new ChangeEventInputPojo();
		changeEventInput.setPostChangeContainer(inputContext.getDbDataContainer());
		return changeEventInput;
	}

}
