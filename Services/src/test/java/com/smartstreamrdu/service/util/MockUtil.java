/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: MockUtil.java
 * Author: Rushikesh Dedhia
 * Date: Jul 30, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mockito.Mockito;

import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.GroupAttribute;
import com.smartstreamrdu.domain.LookupAttributes;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.persistence.repository.FeedConfigurationRepository;
import com.smartstreamrdu.persistence.service.PersistenceService;
import com.smartstreamrdu.service.filter.DataFilterChainService;
import com.smartstreamrdu.service.filter.FilterOutput;
import com.smartstreamrdu.service.inactive.InactiveService;
import com.smartstreamrdu.service.listener.ListenerService;
import com.smartstreamrdu.service.lookup.LookupService;
import com.smartstreamrdu.service.merging.DataContainerMergeException;
import com.smartstreamrdu.service.merging.DataContainerMergingService;
import com.smartstreamrdu.service.normalized.NormalizedValueService;

/**
 * @author Dedhia
 *
 */
public class MockUtil {

	
	public static void createNormalizedService(NormalizedValueService normalizedService) {
		
		DomainType secActive = new DomainType();
		secActive.setVal("1");
		
		Mockito.when(normalizedService.getNormalizedValueForDomainValue(DataAttributeFactory.getAttributeByNameAndLevel("securityStatus", DataLevel.SEC), secActive, "figi")).thenReturn("A");
		Mockito.when(normalizedService.getNormalizedValueForDomainValue(DataAttributeFactory.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS), secActive, "figi")).thenReturn("A");
		
		DomainType specialCaseXrfAdditional = new DomainType();
		specialCaseXrfAdditional.setVal(".*C$");
		specialCaseXrfAdditional.setVal2("AF");
		
		Mockito.when(normalizedService.getNormalizedValueForDomainValue(DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC), specialCaseXrfAdditional, "figi")).thenReturn("Security (Composite)");
		
		DomainType specialCaseXrfAdditional1 = new DomainType();
		specialCaseXrfAdditional1.setVal("val1");
		specialCaseXrfAdditional1.setVal2("MM-Default");
		Mockito.when(normalizedService.getNormalizedValueForDomainValue(DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC), specialCaseXrfAdditional1, "figi")).thenReturn("Default");
		
		DomainType xrfAdditionalDefault = new DomainType();
		xrfAdditionalDefault.setVal("Default");
		xrfAdditionalDefault.setVal2("Default");
		
		Mockito.when(normalizedService.getNormalizedValueForDomainValue(DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC), xrfAdditionalDefault, "figi")).thenReturn("Default");
		
		DomainType exchCode = new DomainType();
		exchCode.setVal("XCME");
		
		Mockito.when(normalizedService.getNormalizedValueForDomainValue(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.SEC), exchCode, "figi")).thenReturn("XCME");
	}
	
	public static void mockInactiveService(InactiveService service) throws Exception{
		Mockito.doNothing().when(service).inactivateIfRequired(Mockito.any());
	}
	
	public static void mock_listenerService_newdataContainer(ListenerService listenerService){
		Mockito.doNothing().when(listenerService).newdataContainer(Mockito.any());
	}

	public static void mock_listenerService_dataContainerMerge(ListenerService listenerService){
		Mockito.doNothing().when(listenerService).dataContainerMerge(Mockito.any(),Mockito.any(),Mockito.any());
	}
	
	public static void mock_listenerService_mergeComplete(ListenerService listenerService){
		Mockito.doNothing().when(listenerService).mergeComplete(Mockito.any(),Mockito.any(),Mockito.any());
	}
	
	public static void mock_listenerService_dataContainerUpdated(ListenerService listenerService){
		Mockito.doNothing().when(listenerService).dataContainerUpdated(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
	}
	
	public static void mock_mergingService_merge_empty(DataContainerMergingService mergingService) throws DataContainerMergeException{
		
		List<DataContainer> containers = new ArrayList<>();
		Mockito.when(mergingService.merge(Mockito.any(), Mockito.any())).thenReturn(containers);
	}
	
	public static void mock_mergingService_merge_val(DataContainerMergingService mergingService) throws DataContainerMergeException{
		
		List<DataContainer> containers = new ArrayList<>();
		containers.add(DataContainerTestUtil.getDataContainer(DataLevel.INS));
		Mockito.when(mergingService.merge(Mockito.any(), Mockito.any())).thenReturn(containers);
	}
	
	public static void mock_persist_service_persist(PersistenceService persistencService){
		Mockito.doNothing().when(persistencService).persist(Mockito.any());
	}
	
	public static void mock_lookupService_getDbDataContainer(LookupService lookupService, DataContainer inpContainer,DomainType feedDs, List<DataContainer> output ) throws Exception{
		Mockito.when(lookupService.getDbDataContainersBySourceUniqueId(inpContainer, feedDs)).thenReturn(output);
	}
	
	
	public static void mock_filterserice_isPersist_true(DataFilterChainService filterService) throws Exception{
		FilterOutput output = new FilterOutput();
		output.setPersist(true);
		Mockito.when(filterService.applyFilterChain(Mockito.any())).thenReturn(output);
		
	}
	
	public static void mock_filterserice_isPersist_false(DataFilterChainService filterService) throws Exception{
		FilterOutput output = new FilterOutput();
		output.setPersist(false);
		Mockito.when(filterService.applyFilterChain(Mockito.any())).thenReturn(output);
		
	}	
	
	public static void mock_lookupService_resolveLookup(LookupService lookupService) throws Exception{
		Mockito.doNothing().when(lookupService).resolveLookup(Mockito.any(), Mockito.any());
	}
	
	
	public static void mock_FeedConfigurationRepository(FeedConfigurationRepository repo){
		FeedConfiguration config = new FeedConfiguration();
		config.setDatasource("trdse");
		config.setFeedName("mockFeed");
		List<String> fileType = new ArrayList<String>();
		fileType.add("XE");
		config.setFileType(fileType);
		List<GroupAttribute> groupAttributes = new ArrayList<>();
		GroupAttribute att1 = new GroupAttribute();
		List<String> attributes = new ArrayList<>();
		attributes.add("assetId");
		att1.setAttributes(attributes );
		att1.setLevel("LE");
		groupAttributes.add(att1);
		config.setGroupAttributes(groupAttributes );
		List<LookupAttributes> lookupAttributes = new ArrayList<>();
		LookupAttributes latt1 = new LookupAttributes();
		List<List<String>> leAtts = new ArrayList<>();
		latt1.setLevel("LE");
		latt1.setAttributes(leAtts);
		lookupAttributes.add(latt1);
		config.setLookupAttributes(lookupAttributes );
		config.setParser("TR_DSE_Parser.xml");
		Mockito.when(repo.findByDatasourceAndFileType("trdse","XE")).thenReturn(config);
	}
	
	public static void createUdmSystemPropertyCache(UdmSystemPropertiesCache cache) {
		Optional<String> value = Optional.of("Security (Composite),Security (Dummy Vendor Ticker)");
		Mockito.when(cache.getPropertiesValue("figiSpecialValuesForAdditionalXrfParameterTypes", "UDL", DataLevel.UDM_SYSTEM_PROPERTIES)).thenReturn(value);
	}
}
