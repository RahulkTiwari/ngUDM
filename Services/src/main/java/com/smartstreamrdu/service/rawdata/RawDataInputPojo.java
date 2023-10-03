/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	RawDataInputPojo.java
 * Author:	Divya Bharadwaj
 * Date:	07-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;

import org.json.simple.JSONObject;

import lombok.Data;

/**
 * @author Bharadwaj
 *
 */
@Data
public class RawDataInputPojo implements Serializable {
	private static final long serialVersionUID = 8163583784719428496L;
	private JSONObject rawData;
	private String dataSourceValue;
	private String rawDataLevelValue;
	private String uniqueColumnValue;
	private String feedExecutionDetailId;
	private String codeHash;
	private String fileType;
}
