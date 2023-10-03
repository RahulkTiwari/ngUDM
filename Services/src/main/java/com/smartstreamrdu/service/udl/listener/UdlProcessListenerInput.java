/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdlProcessListenerInput.java
 * Author:	Padgaonkar S
 * Date:	01-Nov-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.udl.listener;

import com.smartstreamrdu.domain.DataContainer;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @author Padgaonkar
 *
 */
@Data
@Builder
public class UdlProcessListenerInput {

	private DataContainer container;
}
