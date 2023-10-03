/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ListenerEvent.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.listener;

/**
 * @author Jay Sangoi
 *
 */
public enum ListenerEvent {
	
	DataUpdate, DataContainerMerging,NewDataContainer, MergeComplete,NewDomainDataContainer,DomainContainerMerged,DomainContainerUpdate;
}
