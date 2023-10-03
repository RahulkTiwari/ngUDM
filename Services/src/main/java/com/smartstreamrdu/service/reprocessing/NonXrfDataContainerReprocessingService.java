/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : NonXrfDataContainerReprocessingService.java
 * Author :SaJadhav
 * Date : 07-Jun-2021
 */
package com.smartstreamrdu.service.reprocessing;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.proforma.ProformaMessage;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.service.xrf.messaging.ProformaMessageGenerator;
import com.smartstreamrdu.util.Constant.Process;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reprocessing service for non-xrf dataSources dataContainers.
 * 
 * @author SaJadhav
 *
 */
@Component
@Slf4j
public class NonXrfDataContainerReprocessingService implements DataContainerReprocessingService {
	
	@Autowired
	@Setter
	private ProformaMessageGenerator proformaMessageGenerator;

	@Autowired
	@Setter
	private ProducerFactory producerFactory;

	@Override
	public void reprocesDataContainer(DataContainer dataContainer, LocalDateTime staticDataUpdateDate, String rduDomain,
			List<String> changedDomainFieldNames) throws UdmBaseException {
		ChangeEventInputPojo changeEventInput = new ChangeEventInputPojo();
		changeEventInput.setPostChangeContainer(dataContainer);
		
		//NOTE: Currently we will send proforma messages for only EN dataSource.
		//For all other non-xrf dataSources(e.g. ANNA,ESMA) a separate JIRA will be created.Remove this  condition 
		//to enable sending messages for all other non-xrf dataSources
		if (!DataLevel.EN.equals(dataContainer.getLevel())) {
			return;
		}

		List<ProformaMessage> listProformaMessages = proformaMessageGenerator.generateMessage(changeEventInput);

		if (CollectionUtils.isEmpty(listProformaMessages)) {
			log.info("Proforma Message generated for DataContainer with _id {} and level {} is empty",
					dataContainer.get_id(), dataContainer.getLevel());
			return;
		}

		listProformaMessages.stream().forEach(this:: sendProformaMessage);

	}

	/**
	 * @param proformaMessage
	 */
	private void sendProformaMessage(ProformaMessage proformaMessage) {

		// Build the kafka message.
		Message input = new DefaultMessage.Builder().data(proformaMessage).process(Process.FileLoad)
				.target(com.smartstreamrdu.util.Constant.Component.PROFORMA_ENGINE).build();

		Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);

		try {
			producer.sendMessage(input);
			log.debug("Message sent to DIS Proforma with details: {}", proformaMessage);
		} catch (Exception e) {
			log.error(String.format("Following error occured while sending proforma message %s", proformaMessage), e);
		}

	}

}
