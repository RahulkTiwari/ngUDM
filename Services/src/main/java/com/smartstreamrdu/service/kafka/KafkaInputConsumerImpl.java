package com.smartstreamrdu.service.kafka;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.kafka.KafkaInputParameter;
import com.smartstreamrdu.kafka.MessageHandler;


/**
 * @author Shreepad Padgaonkar.
 *
 */
@Component
public class KafkaInputConsumerImpl implements KafkaInputConsumer {

	private static final Duration POLL_DURATION = Duration.ofHours(1);

	private static final int COMMIT_RETRIES = 5;

	private static final int COMMIT_RETRY_INTERVAL = 30_000;

	@Autowired
	private KafkaConfiguration configuration;
		
	private static final Logger _logger = LoggerFactory.getLogger(KafkaInputConsumerImpl.class);
	
	private volatile boolean isAlive = true;

	@Override
	public void startConsumer(KafkaInputParameter input) {
		Properties config = configuration.createConsumerConfiguration();
		String groupId = input.getGroupId();
		config.put(com.smartstreamrdu.util.Constant.KafkaConstant.GROUP_ID, groupId);
		_logger.info("Staring Kafka input consumer: {} ", groupId);
		_logger.debug("Kafka input consumer properties : {} ", config);
		Consumer<Long, String>  consumer = new KafkaConsumer<>(config);
		Set<String> topics = input.getTopics();
		consumer.subscribe(topics);
		
		MessageHandler messageHandler = input.getMessageHandler();
		try {
			while (isAlive) {

				// Poll every hour to prevent timeout exceptions from Kafka 
				ConsumerRecords<Long, String> consumerRecords = consumer.poll(POLL_DURATION);
				if (!consumerRecords.isEmpty()) { // Prevents unnecessary
													// logging
					for(ConsumerRecord<Long, String> r : consumerRecords) {
						 processMessage(messageHandler, r, consumer);
					}
				}
			}
		} catch (Exception e) {
			_logger.error("Closing consumer due to an error", e);
		} finally {
			consumer.close();
		}
	}

	/**
	 * @param messageHandler
	 * @param record
	 * @param consumer 
	 * @return 
	 */
	protected void processMessage(MessageHandler messageHandler, ConsumerRecord<Long, String> record, Consumer<Long, String> consumer) {
		try {
			String topic = record.topic();
			int partition = record.partition();

			TopicPartition topicPartition = new TopicPartition(topic, partition);
			long position = consumer.position(topicPartition);
			_logger.debug("Receiving from - topic {} and position {} ", topicPartition, position);
			_logger.trace("Received message: {} ", record);
			
			//committing offset to KafkaTopic as soon as we received message.This changes has been 
			//done on back of UDM-44280.
			commit(record, topicPartition, consumer);
			messageHandler.handleMessage(record);
		} catch (Exception e) {
			_logger.error("Failed to read record", e);
		}
	}

	/**
	 * Attempts N times to commit the transaction in Kafka. Since this is an important
	 * operation, the commit is tried N times with a gap of S seconds
	 * 
	 * @param record
	 * @param partition
	 * @param topicPartition
	 * @param consumer 
	 */
	protected boolean commit(ConsumerRecord<Long, String> record, TopicPartition topicPartition, Consumer<Long, String> consumer) {
		long offset = record.offset() + 1;
		Map<TopicPartition, OffsetAndMetadata> offsets = prepareSave(topicPartition, offset);
		
		int retries = COMMIT_RETRIES;
		do {
			_logger.debug("Kafka Offset committing - partition: {} and offSet: {} ", topicPartition.partition(), offset);
			try {
				consumer.commitSync(offsets);
				return true;
			} catch (Exception e) {
				_logger.error("Commit failed for partition: {} and offSet: {} ", topicPartition.partition(), offset, e);
				sleep(COMMIT_RETRY_INTERVAL);
			}
		} while(--retries > 0);
		
		return false;
	}

	/**
	 * @param ms 
	 * 
	 */
	protected void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e1) {
			_logger.error("InterruptedException", e1);
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * @param topicPartition
	 * @param offset
	 * @return
	 */
	protected Map<TopicPartition, OffsetAndMetadata> prepareSave(TopicPartition topicPartition, long offset) {
		Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
		OffsetAndMetadata oam = new OffsetAndMetadata(offset, "");
		offsets.put(topicPartition, oam);
		return offsets;
	}
	
	@PreDestroy
	public void destroy(){
		isAlive = false;
	}

	
}
