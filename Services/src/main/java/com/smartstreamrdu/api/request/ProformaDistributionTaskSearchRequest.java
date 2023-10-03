/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    ProformaDistributionTaskSearchRequest.java
 * Author:  AThanage
 * Date:    Sep 30, 2021
 *
 *******************************************************************/
package com.smartstreamrdu.api.request;

import com.smartstreamrdu.domain.proforma.DistributionFileType;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Pojo class for Proforma Distribution Task Search Request.
 * @author AThanage
 *
 */
@Data
@RequiredArgsConstructor
public class ProformaDistributionTaskSearchRequest implements Request{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private DistributionFileType fileType;
	private String profile;
	private String filterName;
	private String status;
}
