/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataFilterChainServiceImplTest.java
 * Author:	S Padgaonakar
 * Date:	11-09-2018
 *
 *******************************************************************
 */


package com.smartstreamrdu.service.filter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

/**
 * @author S Padgaonakar
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DataFilterChainServiceImplTest extends AbstractEmbeddedMongodbJunitParent{

	@Autowired
	DataFilterChainService filterService;

	@Test
	public void init() throws Exception {

		FilterInput input = new FilterInput();
		input.setDatasource("trdse");

		List<DataContainer> dbContainers = new ArrayList<>();
		dbContainers.add(new DataContainer(DataLevel.INS, DataContainerContext.builder().build()));
		input.setDbContainers(dbContainers);

		input.setFeedContainer(new DataContainer(DataLevel.INS, DataContainerContext.builder().build()));
		input.setFeedName("feed");

		FilterOutput output = filterService.applyFilterChain(input);

		Assert.assertNotNull(output);
		Assert.assertEquals(true, output.isPersist());
	}

}
