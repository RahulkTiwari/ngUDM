/**
 * 
 */
package com.smartstreamrdu.service.proforma;

import com.smartstreamrdu.persistence.domain.DistributionFilter;

/**
 * @author Vinish Kumar
 *
 */
public interface ProfileFilterService{
	
	public DistributionFilter getProfileFiltersAttributesForProforma(String filterName, String status); 
	
}
