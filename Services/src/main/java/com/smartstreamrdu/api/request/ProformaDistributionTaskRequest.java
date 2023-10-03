/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ProformaDistributionTaskRequest.java
 * Author:  Rushikesh Dedhia
 * Date:    Jun 22, 2020
 *
 *******************************************************************/
package com.smartstreamrdu.api.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.smartstreamrdu.persistence.domain.ProformaDistributionTask;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Pojo class for Proforma Distribution Task Request.
 * @author Dedhia
 *
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class ProformaDistributionTaskRequest implements Request {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@NotNull
	@NonNull
	@Size(min=1)
	private List<ProformaDistributionTask> distributionTask;

}
