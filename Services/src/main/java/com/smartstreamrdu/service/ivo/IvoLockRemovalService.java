/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: IvoLockRemovalService.java
* Author : Padgaonkar
* Date : April 10, 2019
* 
*/
package com.smartstreamrdu.service.ivo;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * 
 * @author Padgaonkar
 *
 */
public interface IvoLockRemovalService {

	/**
	 * This service is used to remove lock values from dbContainer.
	 * For all Attributes from deleted Container
	 * it will search same Attribute in dbContainer and remove its lock value.
	 * @param dataSource 
	 */
	public void removeLockFromDataContainer(DataContainer deletedContainer, DataContainer dbContainer, String dataSource) throws UdmTechnicalException;

}
