/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataFilterChainServiceImpl.java
 * Author:	Jay Sangoi
 * Date:	17-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.filter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class DataFilterChainServiceImpl implements DataFilterChainService{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6988369338235727509L;
	
	@Autowired
	private List<DataFilter> filters;
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.filter.DataFilterChainService#applyFilterChain(com.smartstreamrdu.service.filter.FilterInput)
	 */
	@Override
	public FilterOutput applyFilterChain(FilterInput input) throws UdmTechnicalException {
		FilterOutput doPersist = null;
		for(DataFilter filter:filters){
			
			if(filter.isApplicationForFeed(input.getFeedName())){
				doPersist = filter.doPersist(input.getFeedContainer(), input.getDbContainers(), input.getDatasource());
				if(!doPersist.isPersist()){
					return doPersist;
				}
			}
			
		}
		return doPersist;
	}

}
