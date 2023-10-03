/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DenEventMessageGenerator.java
 * Author:	Padgaonkar S
 * Date:	05-Jan-2022
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.event.msg.generator;

import com.smartstreamrdu.service.den.event.message.DenEventMessageInputPojo;

/**
 * Generic message generator & sender interface.
 * Based on input it create & sends various event messages to DenEventListener.
 * 
 * @author Padgaonkar
 *
 */
public interface DenEventMessageGenerator {

	/**
	 * Generate & send messages to DenEventListener
	 * @param msgGeneratorInput
	 */
	void generateAndSendMessage(DenEventMessageInputPojo msgGeneratorInput);

}
