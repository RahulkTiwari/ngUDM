/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : IvoEnMergeAndPersistVisitor.java
 * Author :SaJadhav
 * Date : 10-Jun-2021
 */
package com.smartstreamrdu.service.ivo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.service.merging.DataContainerMergeException;
import com.smartstreamrdu.service.persist.MergeAndPersistService;

/**
 * @author SaJadhav
 *
 */
@Component
public class IvoEnMergeAndPersistVisitor implements IvoMergeAndPersistVisitor {
	
	@Autowired
	private MergeAndPersistService mergeAndPersistService;
	
	
	@Autowired
	private IvoQueryService ivoQueryService;

	@Override
	public void visit(IvoMergeVisitable element) throws UdmBaseException {

		IvoContainer ivoContainer = element.getIvoContainer();
		Optional<DataContainer> enContainerOptional=ivoContainer.getEnDataContainer();
		if(enContainerOptional.isPresent()){
			DataContainer enContainer = enContainerOptional.get();
			List<DataContainer> dbContainers = ivoQueryService.getMatchingDataContainerByDocumentId(enContainer);
			try {
				mergeAndPersistService.mergeAndPersistSd(ivoContainer.getDataSourceId(), enContainer, dbContainers, null);
			} catch (DataContainerMergeException e) {
				throw new UdmTechnicalException("Error while merging Sd DataContainer with document id:"+enContainer.get_id(), e);
			}
			
		}
	
	}

}
