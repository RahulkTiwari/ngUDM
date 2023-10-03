/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DistributionFilterResponse.java
 * Author :SaJadhav
 * Date : 26-Oct-2020
 */
package com.smartstreamrdu.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.smartstreamrdu.api.response.Response;
import com.smartstreamrdu.persistence.domain.DistributionFilter;

import lombok.Builder;
import lombok.Data;

/**
 * POJO for distribution filter create/update API response
 * 
 * @author SaJadhav
 *
 */
@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class DistributionFilterResponse implements Response {
	private static final long serialVersionUID = -7694908203677363801L;
	private String responseCode;
	private String responseString;
	private String distributionFilterName;
	private DistributionFilter distributionFilter;
}
