/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MessageFilterationServiceTest.java
 * Author:	S Padgaonkar
 * Date:	04-April-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonobject.filter;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.jayway.jsonpath.Criteria;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class MessageFilterationServiceTest {

	@Autowired
	private MessageFilterationService filterService;

	@SuppressWarnings("unchecked")
	@Test
	public void testFilter_whereCriteria() throws Exception {

		JSONObject object = new JSONObject();
		object.put("item", 3);
		object.put("null_item", null);

		JSONObject check1 = new JSONObject();
		check1.put("item1", 3);
		check1.put("null_item1", null);

		object.put("newCheck", check1);
		Criteria cri = Criteria.where("newCheck.item1").is(3);

		boolean applicable = filterService.isApplicable(object, cri);
		Assert.assertTrue(applicable);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilter_InCriteria() throws Exception {

		JSONObject object = new JSONObject();
		object.put("item", 3);
		object.put("null_item", null);

		JSONObject check1 = new JSONObject();
		check1.put("item1", "newValue");
		check1.put("null_item1", null);

		object.put("newCheck", check1);
		
		List<String> valueList = new ArrayList<>();
		valueList.add("newValue");
		
		Criteria cri = Criteria.where("newCheck.item1").in(valueList);

		boolean applicable = filterService.isApplicable(object, cri);
		Assert.assertTrue(applicable);

	}

}