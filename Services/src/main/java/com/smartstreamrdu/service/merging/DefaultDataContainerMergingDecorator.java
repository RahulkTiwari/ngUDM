/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DefaultDataContainerMergingDecorator.java
 * Author:	Jay Sangoi
 * Date:	05-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Jay Sangoi
 *
 */
@Component
@Scope("prototype")
public class DefaultDataContainerMergingDecorator extends DataContainerMergingDecorator{

	public DefaultDataContainerMergingDecorator(DataContainerMerging merging){
		super(merging);
	}
}
