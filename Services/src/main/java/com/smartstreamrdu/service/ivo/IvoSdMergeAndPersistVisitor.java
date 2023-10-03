/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoSdMergeVisitor.java
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
 * @author SaJadhav
 *
 */
@Component
public class IvoSdMergeAndPersistVisitor implements IvoMergeAndPersistVisitor {

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
		Optional<DataContainer> sdContainerOptional=ivoContainer.getSdContainer();
		if(sdContainerOptional.isPresent()){
			DataContainer sdContainer = sdContainerOptional.get();
			List<DataContainer> dbContainers = ivoQueryService.getMatchingDataContainerByDocumentId(sdContainer);
			try {
				mergeAndPersistService.mergeAndPersistSd(ivoContainer.getDataSourceId(), sdContainer, dbContainers, null);
			} catch (DataContainerMergeException e) {
				throw new UdmTechnicalException("Error while merging Sd DataContainer with document id:"+sdContainer.get_id(), e);
			}
			
		}
	}
	
}
