/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoLeMergeVisitor.java
 * Author : SaJadhav
 * Date : 18-Feb-2019
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
 * @author SaJadhav
 *
 */
@Component
public class IvoLeMergeAndPersistVisitor implements IvoMergeAndPersistVisitor {
	
	@Autowired
	private MergeAndPersistService mergeAndPersistService;

	@Autowired
	private IvoQueryService ivoQueryService;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.visitor.Visitor#visit(java.lang.Object)
	 */
	@Override
	public void visit(IvoMergeVisitable element) throws UdmTechnicalException{
		IvoContainer ivoContainer = element.getIvoContainer();
		Optional<DataContainer> sdLeContainerOptional=ivoContainer.getSdLeContainer();
		if(sdLeContainerOptional.isPresent()){
			DataContainer sdLeContainer = sdLeContainerOptional.get();

			List<DataContainer> dbContainers = ivoQueryService.getMatchingLegalEntityDataContainerFromSdData(sdLeContainer);
			try {
				mergeAndPersistService.mergeAndPersistSd(ivoContainer.getDataSourceId(), sdLeContainer, dbContainers, null);
			} catch (DataContainerMergeException e) {
				throw new UdmTechnicalException("exception while merging Legal Entity", e);
			}
		}
	}
}

