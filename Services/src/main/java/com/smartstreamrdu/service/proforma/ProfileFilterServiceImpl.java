/**
 * 
 */
package com.smartstreamrdu.service.proforma;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.persistence.domain.DistributionFilter;
import com.smartstreamrdu.persistence.repository.ProfileDistributionFilterRepository;

/**
 * @author Vinish Kumar
 * 
 * Access ProfileDistributionFilterRepository to access set/subset of proformaFilter
 *
 */
@Component
public class ProfileFilterServiceImpl implements ProfileFilterService {

	@Autowired
	private ProfileDistributionFilterRepository profileDistributionFilterRepository;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.proforma.ProfileFilterService#getProfileSubscriptionFiltersAttributes(java.lang.String)
	 */
	@Override
	public DistributionFilter getProfileFiltersAttributesForProforma(String filterName, String status) {
		return profileDistributionFilterRepository.findProfileFilters(filterName , status);
	}

}
