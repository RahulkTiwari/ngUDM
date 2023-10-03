/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	EventListenerFactory.java
 * Author:	Padgaonkar S
 * Date:	30-Nov-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.event.process.listener;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections15.map.MultiKeyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.smartstreamrdu.persistence.domain.EventConfiguration;
import com.smartstreamrdu.persistence.repository.EventConfigurationRepository;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.event.enrichment.services.EnrichmentService;

import lombok.Setter;

/**
 * Generic factory which returns all applicable listeners for input dataSource & event.
 * Ideally this should be done when we create UdlWorkFlowInput.But due to
 * limitations of SparkJobs(i.e. in that case we need to serialize all
 * services.) we are adding it over here.
 * 
 * Ideal listener design pattern should not follow this.
 * 
 * @author Padgaonkar
 *
 */
@Component
public class EventListenerFactory {

	@Autowired
	@Setter
	private EventConfigurationRepository repo;

		
	@Setter
	private MultiKeyMap<String,List<? extends ProcessListener>> dataSourceEventVsProcessListenersMap =new MultiKeyMap<>();
	
	/**
	 * return all applicable UdlProcessListener for corresponding dataSource & event
	 * 
	 * @param dataSource
	 * @param string
	 * @return
	 */
	@SuppressWarnings("unchecked")                                   
	public <T extends ProcessListener> List<T> getApplicableListeners(String dataSource, String event) {

		List<T> processListeners = new ArrayList<>();
		
		if(!dataSourceEventVsProcessListenersMap.containsKey(dataSource, event)) {
			
			List<EnrichmentService> enrichmentServices = getApplicableEnrichmentBean(dataSource);
			
			for (EnrichmentService enrichSrv : enrichmentServices) {

				if (enrichSrv.register(event)) {
					processListeners.add((T) enrichSrv);
				}
			}
			dataSourceEventVsProcessListenersMap.put(dataSource, event,processListeners);	
		}
		return (List<T>) dataSourceEventVsProcessListenersMap.get(dataSource, event);
	}


	/**
	 * This method returns all applicable EnrichmentService based on
	 * dataSource.
	 * 
	 * @param dataSource
	 * @return
	 */
	private List<EnrichmentService> getApplicableEnrichmentBean(String dataSource) {
		List<EnrichmentService> enrichmentServices = new ArrayList<>();

		List<EventConfiguration> configs = repo.findEventConfigurationsBasedOnDataSource(dataSource);
		for (EventConfiguration config : configs) {
			String enrichmentBean = config.getEnrichmentBean();
			Object enrichBean = SpringUtil.getBean(enrichmentBean);

			if (enrichBean instanceof EnrichmentService) {
				EnrichmentService enrichSrv = (EnrichmentService) enrichBean;
				enrichSrv.setEventConfiguration(config);
				enrichmentServices.add(enrichSrv);
			}
		}
		return enrichmentServices;
	}
}
