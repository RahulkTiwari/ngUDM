/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	Visitor.java
 * Author:	Jay Sangoi
 * Date:	20-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.visitor;

import com.smartstreamrdu.exception.UdmBaseException;

/**
 * Generic interface for Visitor design pattern
 * 
 * @author Jay Sangoi
 *
 */
public interface Visitor<E> {

	/**
	 * The implementation needs to implement the concrete visit method
	 * 
	 * @param element
	 * @throws Exception 
	 */
	void visit(E element) throws UdmBaseException;
}
