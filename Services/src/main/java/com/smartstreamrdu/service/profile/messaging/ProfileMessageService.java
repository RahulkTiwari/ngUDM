/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ProfileMessageService.java
 * Author:	GMathur
 * Date:	27-Apr-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.profile.messaging;

import java.util.List;

import com.smartstreamrdu.domain.message.ProfileProcessingMessage;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.Profile;

/**
 * Service to generate profile processing messages and send it to DEN queue.
 */
public interface ProfileMessageService {

	/**
	 * Generate kafka messages for profile processing.
	 * 
	 * @param profileList
	 * @return
	 */
	public List<ProfileProcessingMessage> generateProfileProcessingMessage(List<Profile> profileList);
	
	/**
	 * Send ProfileProcessingMessage to 'ProfileProcessingQueue' at DEN.
	 * This message will have profile name and their primary dataSources.
	 * 
	 * UdmTechnicalException will be thrown if any exception comes while sending messages to queue.
	 * 
	 * @param message
	 * @throws UdmTechnicalException
	 */
	public void sendMessage(ProfileProcessingMessage message) throws UdmTechnicalException;
}
