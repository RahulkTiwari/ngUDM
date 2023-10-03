/**
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : EnrichmentRuleServiceInput.java
 * Author :SaJadhav
 * Date : 31-Mar-2022
 */
package com.smartstreamrdu.service.rules.enrichment;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.rules.RuleServiceInput;

import lombok.Data;

/**
 * @author SaJadhav
 *
 */
@Data
public class EnrichmentRuleServiceInput implements RuleServiceInput {
	
	private static final long serialVersionUID = 6190209877443983716L;
	
	private DataContainer dataContainer;
	
	private DataAttribute dataAttribute;

}
