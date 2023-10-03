/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ProfileMessageServiceImpl.java
 * Author:	GMathur
 * Date:	27-Apr-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.profile.messaging;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.message.ProfileProcessingMessage;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.Profile;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.util.Constant.Process;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProfileMessageServiceImpl implements ProfileMessageService{

	@Autowired
	private ProducerFactory producerFactory;
	
	@Override
	public List<ProfileProcessingMessage> generateProfileProcessingMessage(List<Profile> profileList) {
		if(null==profileList || profileList.isEmpty()) {
			log.warn("ProfileMessageService - Empty Profile List");
			return Collections.emptyList();
		}
		return profileList.stream().map(this::createMessage).collect(Collectors.toList());
	}

	@Override
	public void sendMessage(ProfileProcessingMessage message) throws UdmTechnicalException {
		if (null == message) {
			log.info("Empty message : Could not send message for proforma reprocessing.");
			return;
		}
		Message input = new DefaultMessage.Builder().data(message).process(Process.ProfileProcessing)
				.target(com.smartstreamrdu.util.Constant.Component.DATA_ENRICHMENT).build();

		Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);
		try {
			producer.sendMessage(input);
		} catch (Exception e) {
			log.error("Following error occured while sending message for proforma reprocessing for profile {} , exception : {} ", message.getProfileName(), e);
			throw new UdmTechnicalException("Following error occured while sending message for proforma reprocessing for profile : "+message.getProfileName(),e);
		}
		log.info("Message sent to DEN for proforma reprocessing with details: {}", input);
	}

	/**
	 * Convert Profile object to ProfileProcessingMessage.
	 * @param profile
	 * @return
	 */
	private ProfileProcessingMessage createMessage(Profile profile) {
		ProfileProcessingMessage message = new ProfileProcessingMessage();
		
		message.setProfileName(profile.getName());
		message.setPrimaryDataSources(profile.getPrimaryDataSources());
		
		log.debug("Profile message to be processed : {} ",message);
		return message;
	}
}
