/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MessageFilterationServiceImpl.java
 * Author:	S Padgaonkar
 * Date:	04-April-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonobject.filter;

import static com.jayway.jsonpath.Filter.filter;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.internal.path.PredicateContextImpl;

/**
 * 
 * @author Padgaonkar
 *
 */
@Component
public class MessageFilterationServiceImpl implements MessageFilterationService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isApplicable(JSONObject jsonObject, Criteria criteria) {
		return filter(criteria).apply(createPredicateContext(jsonObject));

	}

	/**
	 * This method returns PredicateContext
	 * @param jsonObject
	 * @return
	 */
	public Predicate.PredicateContext createPredicateContext(final Object jsonObject) {
		return new PredicateContextImpl(jsonObject, jsonObject, Configuration.defaultConfiguration(),
				new HashMap<>());
	}
}
