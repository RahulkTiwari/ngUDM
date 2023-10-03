/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoMergeAndPersistService.java
 * Author : SaJadhav
 * Date : 13-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.service.merging.DataContainerMergeException;

/**
 * @author SaJadhav
 *
 */
public interface IvoMergeAndPersistService {
	
/**
 * Merge and persist IVO's with the DB in sDData and sDIvo collection
 * @param ivoContainer
 * @throws DataContainerMergeException 
 * @throws UdmTechnicalException 
 * @throws Exception 
 */
public void mergeAndPersist(IvoContainer ivoContainer) throws UdmTechnicalException;

}
