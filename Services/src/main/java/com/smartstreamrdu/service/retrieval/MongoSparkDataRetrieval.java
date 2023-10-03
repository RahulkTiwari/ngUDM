/**
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: MongoSparkDataRetrieval.java
 * Author : SaJadhav
 * Date : 09-Oct-2018
 * 
 */
package com.smartstreamrdu.service.retrieval;

import java.io.Serializable;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.mongodb.spark.rdd.api.java.JavaMongoRDD;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;

/**
 * @author SaJadhav
 *
 */
public interface MongoSparkDataRetrieval extends Serializable {

	/**
	 * Creates JavaRDD from data in DB based on the criteria and sparkSession
	 * 
	 * @param criteria
	 * @param sparkSession
	 * @return
	 * @throws UdmTechnicalException
	 */
	JavaRDD<DataContainer> retrieveSparkRDD(DataRetrivalInput input, SparkSession sparkSession)
			throws UdmTechnicalException;

	/**
	 * Creates JavaRDD from data in DB based on the criteria and sparkSession
	 * Moreover, this method will filter the returned result and return only those
	 * child containers that satisfy any child level attribute criteria that has
	 * been provided in the input. Filters DataContainer
	 * 
	 * @param criteria
	 * @param sparkSession
	 * @return
	 * @throws UdmTechnicalException
	 */
	JavaRDD<DataContainer> retrieveSparkRDDWithChildFiltering(DataRetrivalInput input, SparkSession sparkSession)
			throws UdmTechnicalException;

	/**
	 * Creates JavaRDD from data in DB based on the DataRetrivalInput and sparkSession 
	 * 
	 * @param criteria
	 * @param sparkSession
	 * @param collectionName
	 * @return
	 * @throws UdmTechnicalException
	 */
	JavaMongoRDD<Document> retrieveSparkDocumentRDD(DataRetrivalInput input, SparkSession sparkSession)
			throws UdmTechnicalException;
	
	/**
	 * Creates JavaRDD from data in DB based on the criteria, sparkSession and
	 * collectionName
	 * 
	 * @param criteria
	 * @param sparkSession
	 * @param collectionName
	 * @return
	 * @throws UdmTechnicalException
	 */
	JavaMongoRDD<Document> retrieveSparkRDD(Criteria criteria, SparkSession sparkSession, String collectionName)
			throws UdmTechnicalException;
	
	/**
	 * Creates JavaRDD from data in DB based on the criteria, sparkSession,
	 * collectionName,Sort and Limit
	 * 
	 * @param criteria
	 * @param sparkSession
	 * @param collectionName
	 * @param query
	 * @return
	 */
	JavaMongoRDD<Document> retrieveSparkRddWithLimitAndSort(Criteria criteria, SparkSession session,
			String collectionName, SortOperation sortOperation, LimitOperation limitOperation);

}
