/*******************************************************************
*
* Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	InsIdNotFoundInactiveContainerListenerInput.java
* Author:Padgaonkar S
* Date:	05-Jan-2022
*
*******************************************************************
*/
package com.smartstreamrdu.service.event.inactive.filter;

import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.service.den.event.message.DenEventMessageInputPojo;

import lombok.Builder;
import lombok.Data;

/**
 * Input class for FilteredInactiveContainerListener.
 * 
 * @author Padgaonkar
 *
 */
@Builder
@Data
public class InsIdNotFoundInactiveContainerListenerInput implements DenEventMessageInputPojo {

	private List<DataContainer> securityDataContainers;

	private String dataSource;

}
