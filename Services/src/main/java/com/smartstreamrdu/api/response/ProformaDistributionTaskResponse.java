/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ProformaDistributionTaskResponse.java
 * Author:  Rushikesh Dedhia
 * Date:    Jun 22, 2020
 *
 ********************************************************************/
package com.smartstreamrdu.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.smartstreamrdu.persistence.domain.ProformaDistributionTask;

import lombok.Builder;
import lombok.Data;

/**
 *  Pojo class for proforma distribution task API response.
 * @author Dedhia
 *
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class ProformaDistributionTaskResponse implements Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4703327693014703362L;
	
	private String responseCode;
	private String responseString;
	private String distributionTaskName;
	private List<ProformaDistributionTask> proformaDistributionTasks;
}
