/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ObjectIdGenerator.java
 * Author:	Jay Sangoi
 * Date:	25-Apr-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.id.generator;

import java.io.Serializable;

/**
 * Interface for generating uniqye object id
 * @author Jay Sangoi
 *
 */
public interface ObjectIdGenerator<T extends Serializable> extends Serializable {
	T generateUniqueId();
}
