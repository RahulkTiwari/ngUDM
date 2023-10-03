/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	OpenFigiIdentifierMapper.java
 * Author:	Shruti Arora
 * Date:	06-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.openfigi;

import java.io.Serializable;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;

public interface OpenFigiIdentifierMapper extends Serializable {

	Serializable getFigiIdentifierValueByName(VfsFigiRequestMessage figiIdentifiers, String name);

	void setFigiIdentifierValueByName(VfsFigiRequestMessage figiIdentifiers, String name, String value);

}
