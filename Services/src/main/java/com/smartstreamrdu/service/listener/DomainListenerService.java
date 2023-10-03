/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainListenerService.java
 * Author : SaJadhav
 * Date : 20-Aug-2019
 * 
 */
package com.smartstreamrdu.service.listener;

import com.smartstreamrdu.domain.DataContainer;

/**
 * @author SaJadhav
 *
 */
public interface DomainListenerService {
	/**
	 * This method propogates the new Domain container addition event to the applicable listeners.
	 * @param dataContainer
	 */
	public void newDomainContainer(DataContainer dataContainer);
	
	/**
	 * This gets called when a updated dta container is merged with DB datacontainer.
	 * It invokes the listeners to update the updDate and updUser
	 * @param dataContainer
	 */
	public void domainConainerMerged(DataContainer dataContainer);
	
	/**
	 * This gets called when merged/new container is persisted in DB.
	 * Postprocessing required after domain update will get invoked by this method.
	 * @param updatedContainer
	 * @param oldContainer
	 */
	public void domainUpdated(DataContainer updatedContainer,DataContainer oldContainer);
}
