/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	StatusFilter.java
 * Author:	Jay Sangoi
 * Date:	16-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.service.status.StatusService;
import com.smartstreamrdu.util.LambdaExceptionUtil;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This filter checks if the status is inactive, then we will not be persisting the data container.
 * @author Jay Sangoi
 *
 */
@Slf4j
@Component
public class StatusFilter implements DataFilter{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3883262723621493215L;
	private String message = "Not persisting the container as %s ";
	
	
	@Autowired
	private StatusService statusService;
	
	@Autowired
	@Setter
	//Ignoring this sonar issue as services should not be Serializable
	private CacheDataRetrieval cacheDataRetrieval;
	
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.filter.DataFilter#isApplicationForFeed(java.lang.String)
	 */
	@Override
	public boolean isApplicationForFeed(String feedName) {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.filter.DataFilter#doPersist(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public FilterOutput doPersist(DataContainer feedContainer, List<DataContainer> dbContainer, String datasource) throws UdmTechnicalException {
		FilterOutput output = new FilterOutput();
		if(CollectionUtils.isEmpty(dbContainer) && feedContainer != null){
			//Check for parent container
			if(statusService.isStatusInactive(feedContainer, datasource)){
				output.setPersist(false);
				output.setMessage(String.format(message,"Status is inactive " ));
				
				//If insStatus is inactive & if it consist of any security 
				//then that security needs to be evaluated before filtering instrument
				//hence adding those into map.[means if same security present in database 
				//with another instrument then that needs to be inactivated(based on vendor)]
				List<DataContainer> allChildDataContainers = feedContainer.getAllChildDataContainers();
				log.info("Adding filtered securities to list:{}",allChildDataContainers);
				output.addFilteredInactiveSecurities(allChildDataContainers);
				return output;
			}
			
			//Check for child container
			 processChildContainers(feedContainer,datasource,output);
			 
			//This check is specifically added for INS level feeds for Scenario
			// such as Ins is active but all listings are InActive.
			String level = cacheDataRetrieval.getDataSourceLevelFromCode(datasource);
			
			if(StringUtils.isNotEmpty(level) && SdDataAttConstant.INSTRUMENT.equals(level)) {
				output.setPersist(true);
			}
			
		}
		else{
			output.setPersist(true);
		}
		return output;
	}
	
	
	private FilterOutput processChildContainers(DataContainer feedContainer, String datasource,FilterOutput output){		//Iterate over all the child data container, 
		//if any of the child container is inactive, then remove from the container. If all the child containers are removed, then don't perssit the container
		
		Map<DataAttribute, List<DataContainer>> childDataContainers = feedContainer.getChildDataContainers();
		if(childDataContainers == null || childDataContainers.isEmpty()){
			output.setPersist(true);
			return output;
		}
		
		AtomicBoolean allInactive = new AtomicBoolean(true);
		StringBuilder messages = new StringBuilder();
		childDataContainers.entrySet().forEach(LambdaExceptionUtil.rethrowConsumer(entry->{
			
			if(!CollectionUtils.isEmpty(entry.getValue())){
				Iterator<DataContainer> iterator = entry.getValue().iterator();
				while(iterator.hasNext()){
					DataContainer con = iterator.next();

					boolean statusInactive = statusService.isStatusInactive(con, datasource) ;
					if(statusInactive){
						
						//Before removing security from instrument we are adding into map
						//to later check whether it consist with any other security & if yes
						//then we inactivate it(based on vendor)
						log.info("Adding filtered security to filterOutput:{}",con);
						addToFilterOutput(con,output);
						
						iterator.remove();
						messages.append("Removing child container ").append(con).append(" as status is inactive").append("\n");
					}
					else{
						allInactive.set(false);
					}
				}
				
			}
			
		}));
		
		output.setPersist(!allInactive.get());
		if(allInactive.get()){
			output.setMessage(String.format(message, "All the child container's are inactive, hence not saving it"));
		}
		else{
			output.setMessage(messages.toString());
		}
		return output;
		
	}

	/**
	 * Adding security to filtered inactive security list
	 * @param con
	 * @param output 
	 */
	private void addToFilterOutput(DataContainer con, FilterOutput output) {
		List<DataContainer> conList = new ArrayList<>();
		conList.add(con);
		output.addFilteredInactiveSecurities(conList);
		
	}
}
