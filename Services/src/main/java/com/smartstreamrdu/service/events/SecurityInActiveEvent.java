/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:    SecurityInActiveEvent.java
 * Author:	Padgaonkar
 * Date:	13-October-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.events;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.smartstreamrdu.events.InactiveBean;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.service.inactive.InactiveService;
import com.smartstreamrdu.service.inactive.InactiveServiceFactory;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This event is get called for Instrument level feeds.In this event
 * if ins is inactive we inactivates its all securities.
 * @author Padgaonkar
 *
 */
@Component
@Slf4j
public class SecurityInActiveEvent implements EventListener<InactiveBean> {

	@Autowired
	@Setter
	private CacheDataRetrieval cache;

	@Setter
	@Autowired
	private InactiveServiceFactory factory;
	

	@Override
	public void propogateEvent(InactiveBean changeEventPojo) {

		InactiveService inactiveService = factory.getSecurityInactiveService(changeEventPojo);
		String dataSourceLevelFromCode = cache.getDataSourceLevelFromCode(changeEventPojo.getDatasource());

		//This service is applicable for INS level feeds only
		if (StringUtils.isNotEmpty(dataSourceLevelFromCode) && dataSourceLevelFromCode.equals(SdDataAttConstant.INSTRUMENT)) {
			try {
				inactiveService.inactivateIfRequired(changeEventPojo);
			} catch (Exception e) {
				log.error("Following exception occured while inActivating securities in SecurityInActiveEvent ", e);
			}
		}
	}

	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return ListenerEvent.DataContainerMerging == event;
	}

	@Override
	public InactiveBean createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		DataContainerMergingChangeEventListenerInputCreationContext inputContext = 
				(DataContainerMergingChangeEventListenerInputCreationContext) inputCreationContext;
		InactiveBean bean = new InactiveBean();
		bean.setDbContainer(inputContext.getDataContainer());
		bean.setDatasource(inputContext.getDataSource());
		return bean;
	}

}
