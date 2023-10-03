package com.smartstreamrdu.service.merging;

import org.springframework.context.annotation.Profile;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@Profile("EmbeddedMongoTest")
public class MergingTestConfiguration extends MongoConfig {
	
}