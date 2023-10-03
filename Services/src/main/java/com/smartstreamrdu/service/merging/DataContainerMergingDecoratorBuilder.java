/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerMergingDecoratorBuilder.java
 * Author:	Jay Sangoi
 * Date:	05-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;

/**
 * Builder for DataContainer merging. It will create the respective Decorator for DataContainer Decorator
 * @Also {@link DataContainerMergingDecorator} 
 * @author Jay Sangoi
 *
 */
public interface DataContainerMergingDecoratorBuilder extends Serializable {
	/**
	 * 
	 * @param feedContainer
	 * @param dbContainers
	 * @return
	 */
	DataContainerMergingDecorator createDataContainerMergingDecorator(DataContainer feedContainer, List<DataContainer> dbContainers);
}
