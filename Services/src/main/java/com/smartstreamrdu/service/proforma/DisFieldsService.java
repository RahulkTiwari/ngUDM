/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DisFieldsCacheService.java
 * Author :SaJadhav
 * Date : 19-Feb-2020
 */
package com.smartstreamrdu.service.proforma;

import java.util.List;

import com.smartstreamrdu.domain.DisDataType;
import com.smartstreamrdu.domain.proforma.ProformaDistributionServiceEnum;
import com.smartstreamrdu.persistence.domain.Field;
import com.smartstreamrdu.rules.DisField;

/**
 * Service for retrieval on disFields collection
 * 
 * @author SaJadhav
 *
 */
public interface DisFieldsService {
	
	/**
	 * Returns the DisField object for the profileField and distributionService (EQUITY/SFTR)
	 * 
	 * @param profileField
	 * @param parentDisField 
	 * @param distributionService
	 * @return
	 */
	public DisField getDisFieldForProfileField(Field profileField,ProformaDistributionServiceEnum distributionService);
	
	/**
	 * Returns List of DisField objects for distributionService (EQUITY/SFTR)
	 * 
	 * @param distributionService
	 * @return
	 */
	public List<DisField> getAllDisFieldsForDistributionService(ProformaDistributionServiceEnum distributionService);
	
	/**
	 * Returns the DisField object for the fieldName and distributionService (EQUITY/SFTR)
	 * 
	 * @param profileField
	 * @param distributionService
	 * @return
	 */
	public DisField getDisFieldByDisFieldNameAndParent(String fieldName,String parentName,ProformaDistributionServiceEnum distributionService);
	
	/**
	 * Returns the DIsDataType  for the fieldName and distributionService (EQUITY/SFTR)
	 * 
	 * @param fieldName
	 * @param distributionService
	 * @return
	 */
	public DisDataType getDataTypeByAttributeName(String fieldName,ProformaDistributionServiceEnum distributionService);
	
}
