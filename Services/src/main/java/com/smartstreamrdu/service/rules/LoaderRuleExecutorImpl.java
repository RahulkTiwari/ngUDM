/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    LoaderRuleExecutorImpl.java
 * Author:  Padgaonkar
 * Date:    Feb 02, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.repository.service.RduRuleRepositoryService;
import com.smartstreamrdu.rules.RduRule;
import com.smartstreamrdu.service.domain.DomainLookupService;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.ListenerConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is used inside merge and persistence layer to invoke rdu rules where ruleType=LoaderRule
 * 
 */
@Component
@Slf4j
public class LoaderRuleExecutorImpl implements LoaderRuleExecutor {

	@Autowired
	private RduRuleRepositoryService repository;
	
	@Autowired
	private DomainLookupService domainService;
	
	@Autowired
	private CacheDataRetrieval cacheDataRetrieve;
	
	private List<RduRule> loaderRules = new ArrayList<>();
 
	/**
	 * load all rules from rduRule collection where ruleType=LoaderRule and cache it
	 */
	@PostConstruct
	private void populateLoaderRuleList() {
		loaderRules = repository.getRduRulesByRuleType(RuleConstants.RULE_TYPE_LOADER_RULE);
	}
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public void executeLoaderRules(List<DataContainer> containers) {
		
		containers.forEach(container ->{
			String dataSource = getDataSourceFromContainer(container);
		
			//This check is added to verify whether container is active or not.If container is inactive
			//we are not processing in further.
			if(!isContainerApplicable(container,dataSource)) {
				log.debug("Loader rules are not executed as container status is inactive or contianer is not applicable: container: {} , dataSource: {}", container, dataSource);
				return;
			}
			
			RduRuleServiceInput input = new RduRuleServiceInput();
			RduRuleContext context = new RduRuleContext();
			context.addToRuleContext(ListenerConstants.datasource, dataSource);
			input.setRduRuleContext(context);
			input.setDataContainer(container);
			RduPostMergeRuleServiceImpl ruleService = new RduPostMergeRuleServiceImpl();
			ruleService.applyRules(input, loaderRules, null);
		});
	}

	/**
	 * This method checks whether input dataContainer status is active or not.
	 * If container is inactive it returns false else it returns true.
	 * @param container
	 * @param dataSource
	 * @return
	 */
	private boolean isContainerApplicable(DataContainer container, String dataSource) {
		String statusFlagForLevel = DataAttributeFactory.getStatusFlagForLevel(container.getLevel());
		
		if(Objects.isNull(statusFlagForLevel)) {
			log.debug("Loader rule are not executed for container:{} as container level is:{}",container,container.getLevel());
			return false;
		}
		DataStorageEnum dataStorage = cacheDataRetrieve.getDataStorageFromDataSource(dataSource);
		DataAttribute statusAttribute = dataStorage.getAttributeByName(statusFlagForLevel);
		
		DomainType statusVal = container.getHighestPriorityValue(statusAttribute);

		if ((statusVal == null) || (statusVal.getNormalizedValue() != null
				&& !statusVal.getNormalizedValue().equals(DomainStatus.ACTIVE))) {
			return false;
		}

		if (statusVal.getNormalizedValue() != null && statusVal.getNormalizedValue().equals(DomainStatus.ACTIVE)) {
			return true;
		}
		String rduDomainForDomainDataAttribute = DataAttributeFactory
				.getRduDomainForDomainDataAttribute(statusAttribute);
		String domainSource = getDomainFromDataSource(dataSource);
		String normalizedValue = (String) domainService.getNormalizedValueForDomainValue(statusVal, domainSource,
				statusVal.getDomain(), rduDomainForDomainDataAttribute);

		return !(normalizedValue == null || normalizedValue.equals(DomainStatus.INACTIVE));
	}

	/**
	 * This method returns dataSource value from dataContainer.
	 * @param container
	 * @return
	 */
	private String getDataSourceFromContainer(DataContainer container) {
		DomainType dataSourceVal = (DomainType) container
				.getHighestPriorityValue(DataAttributeFactory.getDatasourceAttribute(container.getLevel()));
		if (dataSourceVal != null) {
			return dataSourceVal.getVal();
		}
		return null;
	}
	
	/**
	 * This method return domainSource value for given dataSource.
	 * @param dataSource
	 * @return
	 */
	protected String getDomainFromDataSource(String dataSource) {

		try {
			return domainService.getDomainSourceFromDataSource(dataSource);
		} catch (Exception e) {
			log.error("Following error occured while retrieving domainSource for dataSource : {}", dataSource, e);
			return null;
		}

	}

}
