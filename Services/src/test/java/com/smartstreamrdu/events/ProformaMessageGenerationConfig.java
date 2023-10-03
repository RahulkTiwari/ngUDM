package com.smartstreamrdu.events;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.kafka.KafkaProducer;

@Profile("test")
public class ProformaMessageGenerationConfig extends MongoConfig {

	@Bean
	@Primary
	public KafkaProducer KafkaProducer() {
		return Mockito.mock(KafkaProducer.class);
	}
	
}