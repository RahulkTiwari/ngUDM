/**
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : VfsOpenFigiRequestServiceFactory.java
 * Author :SaJadhav
 * Date : 30-Nov-2021
 */
package com.smartstreamrdu.service.openfigi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;

/**
 * Factory for getting instance of VfsOpenFigiRequestService
 * 
 * @author SaJadhav
 *
 */
@Component
public class VfsOpenFigiRequestServiceFactory {
	
	@Autowired
	private FeedUpdatesVfsOpenFigiRequestService feedUpdatesVfsOpenFigiRequestService;
	
	@Autowired
	private IvoUpdatesVfsOpenFigiRequestService ivoUpdatesVfsOpenFigiRequestService;
	
	/**
	 * Factory method to get instance of VfsOpenFigiRequestService based on DataLevel.
	 * Only IVO_INS and INS levels are supported for making the VFS open figi request.
	 * 
	 * @param dataLevel
	 * @return
	 */
	public VfsOpenFigiRequestService getVfsOpenFigiRequestService(DataLevel dataLevel) {
		
		if(DataLevel.IVO_INS.equals(dataLevel)) {
			return this.ivoUpdatesVfsOpenFigiRequestService;
		}else if(DataLevel.INS.equals(dataLevel)){
			return this.feedUpdatesVfsOpenFigiRequestService;
		}
		return null;
	}

}
