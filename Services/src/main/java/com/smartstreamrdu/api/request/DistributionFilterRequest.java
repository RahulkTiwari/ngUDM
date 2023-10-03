/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DistributionFilterRequest.java
 * Author :SaJadhav
 * Date : 26-Oct-2020
 */
package com.smartstreamrdu.api.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.smartstreamrdu.persistence.domain.DistributionFilter;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * POJO for distribution filter create/update API request
 * 
 * @author SaJadhav
 *
 */
@Data
@NoArgsConstructor
public class DistributionFilterRequest implements Request {
	
	private static final long serialVersionUID = 4731170076002201593L;
	
	@NotNull
	@NonNull
	@Size(min=1)
	private List<DistributionFilter> distributionFilter;
}
