/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainContainerChangeEventListenerInputCreationContext.java
 * Author : SaJadhav
 * Date : 23-Sep-2019
 * 
 */
package com.smartstreamrdu.service.events;

import com.smartstreamrdu.domain.DataContainer;

import lombok.Data;

/**
 * Input for DomainContainer change event
 * @author SaJadhav
 *
 */
@Data
public class DomainContainerChangeEventListenerInputCreationContext implements ChangeEventListenerInputCreationContext {
	
	//new domain container or merged domain container
	private DataContainer domainContainer;
	
	//domain container before changes
	private DataContainer oldContainer;
	
}
