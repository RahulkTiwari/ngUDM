/*******************************************************************
*
* Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	InsIdNotFoundFilteredInactiveSecEventMessageGenerator.java
* Author:Padgaonkar S
* Date:	05-Jan-2022
*
*******************************************************************
*/
package com.smartstreamrdu.service.den.event.msg.generator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.events.EventType;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.den.event.listener.DataEnrichmentEvent;
import com.smartstreamrdu.service.den.event.message.DenEventMessageInputPojo;
import com.smartstreamrdu.service.event.inactive.filter.InsIdNotFoundInactiveContainerListenerInput;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class generate message for filtered inactive security event & sends this
 * message to denEventListenerQueue.
 * 
 * @author Padgaonkar
 *
 */
@Slf4j
@Component
public class InsIdNotFoundFilteredInactiveSecEventMessageGenerator implements DenEventMessageGenerator {

	@Setter
	@Autowired
	private DenEventMessageSender msgSender;

	@Override
	public void generateAndSendMessage(DenEventMessageInputPojo msgGeneratorInput) {
		InsIdNotFoundInactiveContainerListenerInput input = (InsIdNotFoundInactiveContainerListenerInput) msgGeneratorInput;
		List<DataContainer> inactiveSecList = input.getSecurityDataContainers();

		for (DataContainer secContainer : inactiveSecList) {

			if (secContainer.getLevel() != DataLevel.SEC) {
				log.info("Returning dataContainer :{} as level is not SEC", secContainer);
				return;
			}

			Map<String, Serializable> eventAttributeMap = new HashMap<>();
			String secSourceUniqueId = secContainer.getHighestPriorityValue(SecurityAttrConstant.SECURITY_SOURCE_UNIQUE_ID);
			eventAttributeMap.put(SecurityAttrConstant.COL_SECURITY_SOURCE_UNIQUE_ID, secSourceUniqueId);
			eventAttributeMap.put(SdDataAttrConstant.COL_DATA_SOURCE, input.getDataSource());

			EventMessage message = new EventMessage();
			message.setEventType(EventType.RECORD_LEVEL_EVENT);
			message.setEventName(DataEnrichmentEvent.INACTIVATION_OF_FILTERED_CONTAINER.getEventName());
			message.setEventAttributes(eventAttributeMap);

			// sending message to denEventListenerQueue
			log.info("Sending message to denEventListenerQueue :{}", message);
			msgSender.createAndsendMessageToEventListenerQueue(message);
		}
	}

}
