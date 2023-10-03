/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	Spark.java
 * Author:	Jay Sangoi
 * Date:	26-Mar-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.spark;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class SparkUtil {
	
	@Value("${spark.master}")
	private String sparkMaster;

	@Value("${application.name}")
	private String name;
	
	public SparkSession getSparkContext() {

		SparkConf conf = new SparkConf().setAppName(name);
		if(sparkMaster != null && !StringUtils.isEmpty(sparkMaster)){
			conf.setMaster(sparkMaster);
		}
		return SparkSession.builder().config(conf).getOrCreate(); 

	}

	public static JavaSparkContext createJavaSparkContext(SparkSession session){
		return JavaSparkContext.fromSparkContext(session.sparkContext());
	}
	
	public static void closeJavaSparkContext(JavaSparkContext context){
		context.close();
	}
	
	public static JavaStreamingContext createStreamContext(SparkSession session, Duration duration){
		return new JavaStreamingContext(createJavaSparkContext(session),duration );
	}

	
}
