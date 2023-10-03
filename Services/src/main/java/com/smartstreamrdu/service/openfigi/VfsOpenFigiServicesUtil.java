package com.smartstreamrdu.service.openfigi;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.util.VfsSystemPropertiesConstant;
import com.smartstreamrdu.util.Constant.VfsConstants;

/**
 * Utility class for the methods which can be 
 * used in Services module for VfsOpenFigi
 * 
 * @author SShetkar
 *
 */
@Component
public class VfsOpenFigiServicesUtil {
	
	@Autowired
	private UdmSystemPropertiesCache systemCache;

	/**
	 * This method return the configure value for key
	 * openfigi.request.param.maturity.days from vfsSystemProperties
	 * 
	 * @return
	 */
	public long getFigiRequestParameterMaturityDay() {
		Optional<String> maturity = systemCache.getPropertiesValue(VfsConstants.FIGI_REQUEST_PARAM_MATURITY_DAYS,
				VfsSystemPropertiesConstant.COMPONENT_VFS, DataLevel.VFS_SYSTEM_PROPERTIES);
		try {
			return maturity.isPresent() ? Long.parseLong(maturity.get()) : VfsConstants.DEFAULT_FIGI_REQUEST_PARAM_MATURITY_DAYS;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid Property 'maturity' specified. Value : " + maturity);
		}
	}
}
