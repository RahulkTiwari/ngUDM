/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: RawRecordContextDetailsPojo.java
 * Author: Rushikesh Dedhia
 * Date: Oct 30, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;
import java.util.List;

import com.smartstreamrdu.domain.Record;

import lombok.Data;

/**
 * @author Dedhia
 *
 */
@Data
public class RawRecordContextDetailsPojo implements Serializable {

	private static final long serialVersionUID = 5817903670646324974L;
	
	private String dataSource;
	private List<String> deltaFieldsByParser;
	private String sourceUniqueIdAttributeName;
	private String feedExecutionDetailId;
	private String rawDataLevelForParser;
	private String fileType;
	private Record record;
	private String sourceUniqueIdAttributeValue;
	private boolean deltaProcessingFlag;//UDM-40688
		
}
