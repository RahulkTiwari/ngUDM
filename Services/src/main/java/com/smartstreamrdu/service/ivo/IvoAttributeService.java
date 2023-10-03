/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoAttributeService.java
 * Author : SaJadhav
 * Date : 03-Apr-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import com.smartstreamrdu.domain.DataAttribute;

/**
 * @author SaJadhav
 *
 */
public interface IvoAttributeService {
	
	/**
	 * Returns the IVO attribute for input sdAttribute.
	 * e.g. for sd attribute at level INS it will return corresponding attribute at level IVO_INS
	 * @param sdAttribute
	 * @return
	 */
	public DataAttribute getIvoAttributeForSdAttribute(DataAttribute sdAttribute);

}
