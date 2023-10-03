/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	AuditserviceImpl.java
 * Author:	Jay Sangoi
 * Date:	19-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.audit;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;

/**
 * 
 * @author Jay Sangoi
 *
 */
@Component
public class AuditServiceImpl implements AuditService {

	private CreateDataContainerAudit craeteDCAuditInfo = this::createDataContainerAudit;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.audit.AuditService#craeateAudit(com.
	 * smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public void createAudit(DataContainer mergedContainer) {
		
		Objects.requireNonNull(mergedContainer, "Data Container cannot be null for populating audit.");
		
		// handle parent level data container audit - to create audit for parent
		// level data container like LE and INS
		createAuditForParentDataContainer(mergedContainer);
		
		// handle child level data container - to create audit for child level
		// data container like SEC
		createAuditForChildDataContainer(mergedContainer); 
		
	}


	/**
	 * handle parent level data container audit - to create audit for parent level data container like LE and INS
	 * @param mergedContainer
	 * @return
	 */
	private void createAuditForChildDataContainer(DataContainer mergedContainer) {
		
		List<DataContainer> childContainers = mergedContainer.getAllChildDataContainers();
		
		if(CollectionUtils.isEmpty(childContainers)){
			return ; 
		}
		
		childContainers.forEach(sc -> craeteDCAuditInfo.create(sc));
		
	}

	/**
	 * handle child level data container - to create audit for child level data container like SEC
	 * @param mergedContainer
	 * @return
	 */
	private void createAuditForParentDataContainer(DataContainer mergedContainer) {
		 craeteDCAuditInfo.create(mergedContainer);
	}
	
	private void createDataContainerAudit(DataContainer mergedContainer){
		/**
		 * If data container is new or there is any update in data container, then directly call  {@link DataContainer.addAudit}
		 */
		if(mergedContainer.isNew() || mergedContainer.hasContainerChanged()){
			mergedContainer.addAudit();
		}
		
	}


	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.audit.AuditService#createAudit(java.util.List)
	 */
	@Override
	public void createAudit(List<DataContainer> mergedContainers) {
		Objects.requireNonNull(mergedContainers, "Data Containers cannot be null for populating audit.");
		
		mergedContainers.forEach(this::createAudit);
	}


}
