/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	KafkaSparkConsumer.java
 * Author:	Jay Sangoi
 * Date:	10-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.kafka;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.ConsumerStrategy;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.OffsetRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.kafka.KafkaOffsetMessage;
import com.smartstreamrdu.persistence.cache.LocalCacheRegister;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.spark.SparkUtil;
import com.smartstreamrdu.util.Constant;

import lombok.extern.slf4j.Slf4j;
import scala.Tuple2;

/**
 * Generic Spark streaming consumer which can talk to Kafka
 * @author Jay Sangoi
 *
 */
@Component
@Slf4j
public class KafkaSparkConsumerImpl implements  KafkaSparkConsumer{
	
	private static final long serialVersionUID = 427217255864578661L;
	
	@Autowired
	private KafkaConfiguration configuration;
	
	@Autowired
	private transient SparkUtil sparkUtil;
	
	@Autowired
    private transient OffsetManager offsetManager;
	
	@PostConstruct
	public void init(){
		if(configuration == null || configuration.createConsumerConfiguration() == null){
			throw new IllegalStateException("Kafka configuration not initialized");
		}
		if(offsetManager==null) {
			offsetManager=SpringUtil.getBean(OffsetManager.class);
		}
	}
	
	private Map<TopicPartition, Long> createPartitionOffset(Collection<String> topics){
				
		List<KafkaOffsetMessage> offsets = new ArrayList<>();
		
		if(CollectionUtils.isEmpty(topics)){
			return Collections.emptyMap();
		}
		
		topics.stream().forEach(sc->{
			List<KafkaOffsetMessage> allOffset = offsetManager.getAllOffset(sc);
			if(CollectionUtils.isNotEmpty(allOffset)){
				offsets.addAll(allOffset);
			}
		});
		
		if(CollectionUtils.isEmpty(offsets)){
			return Collections.emptyMap();
		}
		
		Map<TopicPartition, Long> map = new HashMap<>();
		offsets.forEach(sc -> 
			map.put(new TopicPartition(sc.getTopic(), sc.getPartition()), sc.getOffset())
		);
			
		return map; 
	}
	
	private void persistOffsetRanges(OffsetRange[] offsets){
		if(ArrayUtils.isEmpty(offsets)){
			return;
		}
		List<KafkaOffsetMessage> list = new ArrayList<>(offsets.length);
		for(OffsetRange range:offsets){
			
			list.add(new KafkaOffsetMessage(range.topic(),range.partition(), range.untilOffset()));
			
		}
		offsetManager.saveOffsetInExternalStore(list);
		
	}
	
	@Override
	public <K extends Serializable, V extends Serializable> void start(KafkaSparkConsumerInput<K, V> input) throws InterruptedException{
		String ref = "KafkaSparkConsumer";
		Objects.requireNonNull(input, Constant.NOT_NULL_MESSAGE("KafkaSparkConsumerInput",ref));
		Objects.requireNonNull(input.getTopics(), Constant.NOT_NULL_MESSAGE("getTopics",ref));
		Objects.requireNonNull(input.getGroupId(), Constant.NOT_NULL_MESSAGE("getGroupId",ref));
		SparkSession session = sparkUtil.getSparkContext();
		Map<TopicPartition, Long> topicPartition = createPartitionOffset(input.getTopics());
		log.info("topicPartition:{}",topicPartition);
		 Stream<Entry<Object, Object>> mapstream = configuration.createConsumerConfiguration().entrySet().stream();
		    Map<String, Object> mapOfProperties = mapstream.collect(Collectors.toMap(
		            e -> String.valueOf(e.getKey()),
		            Entry ::getValue));
		    mapOfProperties.put("group.id",input.getGroupId() );
		    Long duration;
		    try {
				duration= Long.parseLong(configuration.createConsumerConfiguration().getProperty("duration"));
			}
			catch(Exception e) {
				throw new IllegalArgumentException("The Value of Interval Duration in the properties is not correct");
			}
			
		JavaStreamingContext createStreamContext = SparkUtil.createStreamContext(session, new Duration(duration));
		ConsumerStrategy<K, V> subscribe;
		if(MapUtils.isEmpty(topicPartition)){
			subscribe = ConsumerStrategies.<K, V>Subscribe(input.getTopics(),mapOfProperties);
		}
		else{
			subscribe = ConsumerStrategies.<K, V>Subscribe(input.getTopics(),mapOfProperties,topicPartition);
		}
		
		JavaInputDStream<ConsumerRecord<K, V>> stream = KafkaUtils.createDirectStream(createStreamContext,
				LocationStrategies.PreferConsistent(), subscribe);
		
		
		stream.foreachRDD(rdd -> {
			if (!rdd.isEmpty()) { // Prevents unnecessary logging
			  OffsetRange[] offsetRanges = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
			  
			  LocalCacheRegister.Accessor.getLocalCache().clearLocalCaches();
			  log.debug("offsetRanges-{}",Arrays.toString(offsetRanges));
			  
			  boolean hasOffsetChanged = hasOffsetChanged(offsetRanges);
			  
			  if(hasOffsetChanged){
				  log.info("Offset has changed, hence processing it {}","");
				  try{
					  Long startTime= System.currentTimeMillis();
					  log.info("Start Processing the Batch {}",startTime);
					  
				  JavaPairRDD<K, V> streamUnwraped = rdd.mapToPair(c -> new Tuple2<K, V>(c.key(), c.value()));
				  BatchHandler<K, V> handler = input.getHandler();
				  handler.handleBatch(streamUnwraped, topics(offsetRanges));
				  Long endTime= System.currentTimeMillis();
				  log.info("End Processing the Batch {}",endTime);
				  log.info("Total Time Taken in MilliSeconds {}",endTime-startTime);
					  
				  }
				  catch(Exception e){
					  log.error("Exception while handling message",e);
				  }
				  finally{
					  log.info("Persisting offset into db :{}", (Object) offsetRanges);
					  persistOffsetRanges(offsetRanges);
				  }
			  }
			}
		});
		
		createStreamContext.start();
		
		createStreamContext.awaitTermination();
		
	}
	
	private Set<String> topics(OffsetRange[] offsetRanges){
		Set<String> topics = new HashSet<>();
		for(OffsetRange ofset:offsetRanges){
			topics.add(ofset.topic());
		}
		return topics;
	}
	
	private boolean hasOffsetChanged(OffsetRange[] offsetRanges ){
		boolean hasChanged = false;
		
		for(OffsetRange ofset:offsetRanges){
			if(ofset.fromOffset() != ofset.untilOffset()){
				hasChanged = true;
				break;
			}
		}
		return hasChanged;		
	}

}
