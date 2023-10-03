/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ParentContainerComparator.java
 * Author: Shruti Arora
 * Date: 29-May-2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

public interface ParentContainerComparator extends Serializable{

	DataContainer compareDataContainer(DataContainer feedDContainer, List<DataContainer> dbContainers) throws UdmTechnicalException;
}
