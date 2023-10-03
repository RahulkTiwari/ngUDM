/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RduRuleListenerInput.java
 * Author:	Padgaonkar S
 * Date:	29-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules.listeners;

import com.smartstreamrdu.domain.DataContainer;

import lombok.Builder;
import lombok.Data;

/**
 * Input class for rdu rule listeners 
 * @author Padgaonkar
 *
 */
@Data
@Builder
public class RduRuleListenerInput {

	private DataContainer feedContainer;
}
