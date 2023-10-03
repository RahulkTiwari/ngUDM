/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    MoodysRatingRankingServiceImpl.java
 * Author:  Dedhia
 * Date:    21-Sep-2020
 *
 ********************************************************************/
package com.smartstreamrdu.service.proforma;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.proforma.MoodysRatingRankings;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

import lombok.Setter;

/**
 * @author Dedhia
 *
 */
@Component
public class MoodysRatingRankingCacheServiceImpl implements MoodysRatingRankingCacheService {
	
	@Setter
	@Autowired
	private  MongoTemplate mongoTemplate;
	
	@SuppressWarnings("resource")
	private void ensureMongoTemplate() {
		if (mongoTemplate == null) {
			mongoTemplate = new AnnotationConfigApplicationContext(MongoConfig.class).getBean(MongoTemplate.class);
		}
	}

	@PostConstruct
	private void init() {
		ensureMongoTemplate();
	}
	
	/**
	 *  {@inheritDoc}
	 */
	@Override
	public List<MoodysRatingRankings> getAllMoodysRatingsRankings() {
		return mongoTemplate.findAll(MoodysRatingRankings.class);
	}

}
