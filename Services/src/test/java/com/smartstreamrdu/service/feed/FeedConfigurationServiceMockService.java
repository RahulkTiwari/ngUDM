/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	FeedConfigurationServiceMockService.java
 * Author:	Jay Sangoi
 * Date:	05-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.feed;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.GroupAttribute;
import com.smartstreamrdu.domain.LookupAttributes;
import com.smartstreamrdu.domain.UdlFeedConfigSearch;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class FeedConfigurationServiceMockService {

	@Autowired
	private FeedConfigurationService feedService;
	
	public void createMockData(){
		
		UdlFeedConfigSearch search = new UdlFeedConfigSearch();
		
		search.setDatasource("trdse");
		search.setFileType("XE");
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
		Mockito.when(feedService.fetchFeedConfigurationForFileLoad(search )).thenReturn(config);
	}
	
	
	
	
}
