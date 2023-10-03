/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	LookupAttributeInput.java
 * Author:	Jay Sangoi
 * Date:	19-Apr-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.lookup.input;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Input for lookup attribute
 * @author Jay Sangoi
 *
 */
@Data
public class LookupAttributeInput implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2167781535669767654L;

	/**
	 * LE Lookup attributes  
	 */
	private List<List<String>> insLeAttributes;
	
	/**
	 * INS+SEC lookup attributes
	 */
	private List<List<String>> insInsAttributes;

	
}
