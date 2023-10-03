/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoSdIvoMergeVisitor.java
 * Author : SaJadhav
 * Date : 13-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.service.merging.DataContainerMergeException;
import com.smartstreamrdu.service.persist.MergeAndPersistService;

/**
 * Merges the sdIvo document with the DB document.
 * @author SaJadhav
 *
 */
@Component
public class IvoSdIvoMergeAndPersistVisitor implements IvoMergeAndPersistVisitor {
		
	@Autowired
	private MergeAndPersistService mergeAndPersistService;
	
	@Autowired
	private IvoQueryService queryService;
	

	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.visitor.Visitor#visit(java.lang.Object)
	 */
	@Override
	public void visit(IvoMergeVisitable element) throws UdmTechnicalException{
		IvoContainer ivoContainer = element.getIvoContainer();
		Optional<DataContainer> sdIvoContainerOptional=ivoContainer.getSdIvoContainer();
		if(sdIvoContainerOptional.isPresent()){
			DataContainer sdIvoContainer = sdIvoContainerOptional.get();
			List<DataContainer> dbContainers = queryService.getMatchingDataContainerFromSdIvo(sdIvoContainer);
			try {
				mergeAndPersistService.mergeAndPersistSd(ivoContainer.getDataSourceId(), sdIvoContainer, dbContainers, null);
			} catch (DataContainerMergeException e) {
				throw new UdmTechnicalException("Exception while merging Sd Ivo DataContainer", e);
			}
		}
	}




}
