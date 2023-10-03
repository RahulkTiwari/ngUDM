
package com.smartstreamrdu.service.ivo;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.listener.ListenerService;


public class IvoLockConfig extends MongoConfig{
	
	@Bean
	@Primary
	public ListenerService listenerService() {
		return Mockito.mock(ListenerService.class);
	}

}
