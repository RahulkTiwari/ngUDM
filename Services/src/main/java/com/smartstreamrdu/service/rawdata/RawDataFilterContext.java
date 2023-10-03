/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RawDataFilterContext.java
 * Author:  Padgaonkar
 * Date:    May 28, 2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rawdata;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;



/**
 * This class holds information required to filter rawData.
 * @author Padgaonkar
 *
 */
@Data
@Builder
public class RawDataFilterContext implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * This is initial startDate of file which is reprocessing.
	 * {@link RawDataReprocessingFilter}
	 */
	private LocalDateTime pendingFileProcessingStartDate;
}
