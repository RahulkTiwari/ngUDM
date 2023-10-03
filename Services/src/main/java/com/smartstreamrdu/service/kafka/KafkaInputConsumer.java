package com.smartstreamrdu.service.kafka;

import com.smartstreamrdu.kafka.KafkaInputParameter;

/**
 * @author Shreepad Padgaonkar.
 *
 */
public interface KafkaInputConsumer {

	/**
	 * Method used to set input parameter for kafka
	 * @param KafkaInputParameter
	 */
	void startConsumer(KafkaInputParameter input) ;
 
}
