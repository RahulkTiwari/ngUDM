/**
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: MongoSparkUtil.java
 * Author : SaJadhav
 * Date : 04-Oct-2018
 * 
 */
package com.smartstreamrdu.service.spark;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.spark.MongoClientFactory;
import com.mongodb.spark.MongoConnector;
import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.config.MongoSharedConfig;
import com.mongodb.spark.config.ReadConfig;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.mongodb.codecs.CodecRegistryHelper;
import com.smartstreamrdu.persistence.service.PersistenceEntityRepository;

/**
 * @author SaJadhav
 *
 */
@Component
public class MongoSparkUtil {
	
	@Autowired
	private PersistenceEntityRepository entityRepository; 
	
	public MongoSpark getMongoSpark(DataLevel dataLevel,SparkSession sparkSession){
		JavaSparkContext jsc = SparkUtil.createJavaSparkContext(sparkSession);
		ReadConfig readConfig = getReadConfig(jsc, dataLevel);
		
		MongoConnector connector = new MongoConnector(new CustomMongoClientFactory(
	            ReadConfig.stripPrefix(readConfig.asOptions()).get(MongoSharedConfig.mongoURIProperty()).get()));
		return MongoSpark.builder().connector(connector).javaSparkContext(jsc).readConfig(readConfig).build();
	}
	
	private ReadConfig getReadConfig(JavaSparkContext jsc, DataLevel dataLevel) {
		String className = entityRepository.getRootClassForLevel(dataLevel).getSimpleName();
		String collectionName=className.substring(0, 1).toLowerCase() + className.substring(1, className.length());
		Map<String, String> readOverrides = new HashMap<>();
		readOverrides.put("collection", collectionName);
		return ReadConfig.create(jsc).withOptions(readOverrides);
	}

	public MongoSpark getMongoSpark(String className, SparkSession sparkSession) {
		JavaSparkContext jsc = SparkUtil.createJavaSparkContext(sparkSession);
		ReadConfig readConfig = getReadConfig(jsc, className);

		MongoConnector connector = new MongoConnector(new CustomMongoClientFactory(
				ReadConfig.stripPrefix(readConfig.asOptions()).get(MongoSharedConfig.mongoURIProperty()).get()));
		return MongoSpark.builder().connector(connector).javaSparkContext(jsc).readConfig(readConfig).build();
	}

	private ReadConfig getReadConfig(JavaSparkContext jsc, String className) {
		String collectionName = className.substring(0, 1).toLowerCase() + className.substring(1, className.length());
		Map<String, String> readOverrides = new HashMap<>();
		readOverrides.put("collection", collectionName);
		return ReadConfig.create(jsc).withOptions(readOverrides);
	}
	
	 /**
	 * Custom mongoClientFactory which uses provided codecRegistry to encode/decode Java Objects into BSON
	 *
	 */
	private static class CustomMongoClientFactory implements MongoClientFactory {

			private static final long serialVersionUID = -9033892144353851122L;
			private String mongoURI;

	        public CustomMongoClientFactory(String mongoURI) {
	            this.mongoURI = mongoURI;
	        }

			/* (non-Javadoc)
			 * @see com.mongodb.spark.MongoClientFactory#create()
			 */
			@Override
			public com.mongodb.client.MongoClient create() {
				ConnectionString string = new ConnectionString(mongoURI);
				 Builder builder = MongoClientSettings.builder();
				 MongoClientSettings clientSetting = builder.applyConnectionString(string).codecRegistry(CodecRegistryHelper.getCodecRegistry()).build(); 
				 return new MongoClientImpl(clientSetting, null);
			}
	    }
}
