/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	KafkaConfiguration.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.kafka;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.util.Constant.Process;
import com.smartstreamrdu.util.UdmSystemPropertiesConstant;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class KafkaConfigurationImpl implements KafkaConfiguration{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3894655756859837795L;
	
	@Autowired
	private transient UdmSystemPropertiesCache systemCache;
	
	
	@Value("${heartbeatInterval}")
	private String heartbeatInterval;
	
	@Value("${sessionTimeout}")
	private String sessionTimeout; 
	
	@Value("${intervalDuration}")
	private String intervalDuration;
	
	@Value("${maxPollIntervalMs:86400000}") 
	private String maxPollIntervalMs; //Default 1 day
	
	@Value("${producerRetry:10}")
	private Integer producerRetry;
	
	@Value("${requestTimeOutMs:600000}")
	private Integer requestTimeOutMs;
	
	@Value("${default.api.timeout.ms:120000}")
	private Integer defaultApiTimeout;

	private String kafkaServer;
		
	private List<String> components;

	private List<String> topicList;
			
	private String figiRequestQueue;
	
	private String disQueue;
	
	private String xrfDeadLetterQueue;
	
	private String vfsRetryQueue;
	
	private String dataEnrichmentQueue;
	
	private String udmExceptionQueue;
	
	private String proformaDeadLetterQueue;
	
	private String streamingDataLoadQueue;
	
	private String profileProcessingQueue;
	
	private String domainChangeReprocessingQueue;
	
	private String leUpdateProformaProcessingQueue;
	
	private String instrumentInactivationQueue;
	
	//Proforma sends notifications on this queue for each event based on Profile.
	private String eventNotificationQueue;
	
	private String denEventListenerQueue ;
	
	private String figiPeriodicRefreshQueue;

	
	/**
	 * Since each message can take a few mins to hours to consume and we dont want to commit 
	 * before the message is processed, this should be set to 1. 
	 */
	@Value("${maxPollRecords:1}")
	private String maxPollRecords;  
	
	private static final Logger _logger = LoggerFactory.getLogger(KafkaConfigurationImpl.class);
	
	private Map<com.smartstreamrdu.util.Constant.Component, String> topics = new EnumMap<>(com.smartstreamrdu.util.Constant.Component.class);


	@PostConstruct
	public void init() {
		initialize();		
		if(components!=null && !components.isEmpty()) {
			for(int i=0;i<components.size();i++){
				topics.put(com.smartstreamrdu.util.Constant.Component.valueOf(components.get(i)), topicList.get(i));
			}		
		}
		_logger.info("Topics :{}",topics);

	}
	
	private void initialize() {		
			
	
		Optional<String> disQueueOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.DIS_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(disQueueOptional.isPresent()) {
			disQueue = disQueueOptional.get();
		}
		
		Optional<String> xrfDeadLetterQueueOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.XRF_DEADLETTER_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(xrfDeadLetterQueueOptional.isPresent()) {
			xrfDeadLetterQueue = xrfDeadLetterQueueOptional.get();
		}
		
		Optional<String> figiRequestQueueOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.FIGI_REQUEST_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(figiRequestQueueOptional.isPresent()) {
			figiRequestQueue = figiRequestQueueOptional.get();
		}
		
		Optional<String> vfsRetryQueueOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.VFS_RETRY_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(vfsRetryQueueOptional.isPresent()) {
			vfsRetryQueue = vfsRetryQueueOptional.get();
		}
		
		Optional<String> dataEnrichmentQueueOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.DATA_ENRICHMENT_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(dataEnrichmentQueueOptional.isPresent()) {
			dataEnrichmentQueue = dataEnrichmentQueueOptional.get();
		}
		
		Optional<String> udmExceptionQueueOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.UDM_EXCEPTION_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(udmExceptionQueueOptional.isPresent()) {
			udmExceptionQueue = udmExceptionQueueOptional.get();
		}
		
		Optional<String> kafkaServerOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.KAFKA_SERVER_URL,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(kafkaServerOptional.isPresent()) {
			kafkaServer = kafkaServerOptional.get();
		}
		
		Optional<String> kafkaTopics = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.TOPICS,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(kafkaTopics.isPresent()) {
			topicList = Arrays.asList(kafkaTopics.get().split(","));
		}
		
		Optional<String> kafkaComponents = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.COMPONENTS,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(kafkaComponents.isPresent()) {
			components = Arrays.asList(kafkaComponents.get().split(","));
		}
		
		Optional<String> proformaDeadLetterQ = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.PROFORMA_DEADLETTER_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(proformaDeadLetterQ.isPresent()) {
			proformaDeadLetterQueue = proformaDeadLetterQ.get();
		}
		
		Optional<String> streamingDataLoadQ = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.STREAMING_DATA_LOAD_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(streamingDataLoadQ.isPresent()) {
			streamingDataLoadQueue = streamingDataLoadQ.get();
		}
		
		Optional<String> profileProcessingQ = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.PROFILE_PROCESSING_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(profileProcessingQ.isPresent()) {
			profileProcessingQueue  = profileProcessingQ.get();
		}
		
		Optional<String> domainReprocessingQ = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.DOMAIN_CHANGE_REPROCESSING_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(domainReprocessingQ.isPresent()) {
			domainChangeReprocessingQueue  = domainReprocessingQ.get();
		}
		
		Optional<String> leUpdateProformaProcessingQ = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.LE_UPDATE_PROFORMA_PROCESSING_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(leUpdateProformaProcessingQ.isPresent()) {
			leUpdateProformaProcessingQueue  = leUpdateProformaProcessingQ.get();
		}
		
		Optional<String> instrumentInactivationQ = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.INSTRUMENT_INACTIVATION_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(instrumentInactivationQ.isPresent()) {
			instrumentInactivationQueue  = instrumentInactivationQ.get();
		}
		
		Optional<String> eventNotificationQueueQ = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.ENS_CLIENT_DISTRIBUTION,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(eventNotificationQueueQ.isPresent()) {
			eventNotificationQueue  = eventNotificationQueueQ.get();
		}
		
		Optional<String> denEventListenerQueueQ = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.DEN_EVENT_LISTENER,DataLevel.UDM_SYSTEM_PROPERTIES);
		if(denEventListenerQueueQ.isPresent()) {
			denEventListenerQueue  = denEventListenerQueueQ.get();
		}
		
		Optional<String> figiPeriodicRefreshQueueOptional = systemCache.getPropertiesValue(UdmSystemPropertiesConstant.FIGI_PERIODIC_REFRESH_QUEUE,DataLevel.UDM_SYSTEM_PROPERTIES);
        if(figiPeriodicRefreshQueueOptional.isPresent()) {
            figiPeriodicRefreshQueue = figiPeriodicRefreshQueueOptional.get();
        }
		
	}

	@Override
	public Properties createProducerConfiguration() {
		Properties kafkaProderProperties = new Properties();

		if (kafkaServer != null) {
			kafkaProderProperties.put("bootstrap.servers", kafkaServer);
		}
		kafkaProderProperties.put("acks", "all");
		kafkaProderProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		kafkaProderProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		kafkaProderProperties.put(ProducerConfig.RETRIES_CONFIG, producerRetry);
		kafkaProderProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeOutMs);
		return kafkaProderProperties;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.loader.spark.kafka.KafkaConfiguration#getTopicName(com.smartstreamrdu.util.Constant.Component, com.smartstreamrdu.util.Constant.Component)
	 */
	@Override
	public String getTopicName(com.smartstreamrdu.util.Constant.Process process, com.smartstreamrdu.util.Constant.Component source,
			com.smartstreamrdu.util.Constant.Component target) {
		//TODO : read from db 
		
		if(process == Process.FileLoad || process == null){
			
			return topics.get(target);
		}
		
		if(process == Process.FigiRequest){
			return figiRequestQueue;
		}

		if(process == Process.DistributionStart){
			return disQueue;
		}
		
		if(process == Process.XRFFailure) {
			return xrfDeadLetterQueue;
		}
		
		if(process == Process.VfsRetry){
			return vfsRetryQueue;
		}
		
		if(process == Process.LinkageJob || process == Process.RduLockRemovalJob){
			return dataEnrichmentQueue;
		}

		if(process == Process.UdmException){
			return udmExceptionQueue;
		}
		
		if(process == Process.ProformaFailture){
			return proformaDeadLetterQueue;
		}
		
		if(process == Process.StreamingDataLoad){
			return streamingDataLoadQueue;
		}
		
		if(process == Process.ProfileProcessing){
			return profileProcessingQueue;
		}
		
		if(process == Process.DOMAIN_CHANGE_REPROCESSING){
			return domainChangeReprocessingQueue;
		}
		
		if(process == Process.LE_UPDATE_PROFORMA_PROCESSING){
			return leUpdateProformaProcessingQueue;
		}
		
		if(process == Process.INSTRUMENT_INACTIVATION){
			return instrumentInactivationQueue;
		}
		
		if(process == Process.EVENT_NOTIFICATION){
			return eventNotificationQueue;
		}
		
		if(process == Process.DEN_EVENT){
			return denEventListenerQueue;
		}
		
		if(process == Process.PERIODIC_REFRESH){
            return figiPeriodicRefreshQueue;
        }

		return null;
		
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.loader.spark.kafka.KafkaConfiguration#getConsumerConfiguration()
	 */
	@Override
	public Properties createConsumerConfiguration() {
		Properties kafkaConsumerProperties = new Properties();

		if (kafkaServer != null) {
			kafkaConsumerProperties.put("bootstrap.servers", kafkaServer);
		}

		kafkaConsumerProperties.put("key.deserializer", StringDeserializer.class);
		kafkaConsumerProperties.put("value.deserializer", StringDeserializer.class);
		kafkaConsumerProperties.put("auto.offset.reset", "earliest");
		kafkaConsumerProperties.put("enable.auto.commit", false);
		kafkaConsumerProperties.put("heartbeat.interval.ms", heartbeatInterval);
		kafkaConsumerProperties.put("session.timeout.ms", sessionTimeout);
		kafkaConsumerProperties.put("max.poll.interval.ms", maxPollIntervalMs);
		kafkaConsumerProperties.put("max.poll.records", maxPollRecords);
		kafkaConsumerProperties.put("duration", intervalDuration);
		kafkaConsumerProperties.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, defaultApiTimeout);

		return kafkaConsumerProperties;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.loader.spark.kafka.KafkaConfiguration#getPollTimeForComponent(com.smartstreamrdu.util.Constant.Component)
	 */
	@Override
	public long getPollTimeForComponent(com.smartstreamrdu.util.Constant.Component component) {
		return 1000;
	}
	

}
