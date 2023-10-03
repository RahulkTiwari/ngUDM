/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: IvoLockRemovalEvent.java
* Author : VRamani
* Date : Mar 12, 2019
* 
*/
package com.smartstreamrdu.service.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.message.IvoLockRemovalMessage;
import com.smartstreamrdu.domain.message.UdmMessageKey;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.util.Constant.ListenerConstants;
import com.smartstreamrdu.util.Constant.Process;

/**
* @author VRamani
*
*/
@Component
public class IvoLockRemovalEvent implements EventListener<ChangeEventInputPojo> {
	
	static final Logger _logger = LoggerFactory.getLogger(IvoLockRemovalEvent.class);
	private static final DataAttribute dataSourceAttr = DataAttributeFactory
			.getAttributeByNameAndLevel(ListenerConstants.dataSource, DataLevel.Document);
	private static final DataAttribute instrumentId = DataAttributeFactory
			.getAttributeByNameAndLevel(ListenerConstants.instrumentId, DataLevel.INS);
	
	@Autowired
	private ProducerFactory producerFactory;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#propogateEvent(java.io.Serializable)
	 */
	@Override
	public void propogateEvent(ChangeEventInputPojo changeEventPojo) {
		DataContainer container = changeEventPojo.getPostChangeContainer();
		if (container.hasContainerChanged() && !container.isNew() && DataLevel.INS.equals(container.getLevel())) {
			IvoLockRemovalMessage message = createIvoLockRemovalMessage(container);
			UdmMessageKey udmMessageKey = UdmMessageKey.builder().action(Process.RduLockRemovalJob.name())
					.variableAttributeValue(message.getDocumentId()).build();
			Message input = new DefaultMessage.Builder().data(message).key(udmMessageKey)
					.target(com.smartstreamrdu.util.Constant.Component.DATA_ENRICHMENT).process(Process.RduLockRemovalJob)
					.build();

			Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);

			try {
				producer.sendMessage(input);
				_logger.debug("Message sent to Data Enrichment with details: {}", input);
			} catch (Exception e) {
				_logger.error("Following error occured while sending message to Data Enrichment", e);
			}
		}
	}

	/**
	 * @param container
	 * @return message
	 */
	@SuppressWarnings("unchecked")
	private IvoLockRemovalMessage createIvoLockRemovalMessage(DataContainer container) {
		IvoLockRemovalMessage message = new IvoLockRemovalMessage();
		message.setDocumentId(container.get_id());
		DataValue<DomainType> dataSourceVal = (DataValue<DomainType>) (container.getAttributeValue(dataSourceAttr));
		message.setDataSource(dataSourceVal.getValue().getVal());
		DataValue<String> instrumentIdDataValue = (DataValue<String>) container.getAttributeValue(instrumentId);
		message.setObjectId((String) instrumentIdDataValue.getValue(LockLevel.FEED));

		return message;
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
		
		ChangeEventInputPojo changeEventInputPojo = new ChangeEventInputPojo();
		changeEventInputPojo.setPostChangeContainer(inputContext.getDbDataContainer());
		return changeEventInputPojo;
	}

}
