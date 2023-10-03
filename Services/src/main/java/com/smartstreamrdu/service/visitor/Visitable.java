/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	Visitable.java
 * Author:	Jay Sangoi
 * Date:	20-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.visitor;

/**
 * This is a general Visitable interface for Visitor design pattern
 * @author Jay Sangoi
 *
 */
public interface Visitable<E> {
	/**
	 * 
	 * @param visitor
	 * @throws Exception 
	 */
	void accept(Visitor<E> visitor) throws Exception;
}
