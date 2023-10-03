package com.smartstreamrdu.service.events;

import com.smartstreamrdu.domain.DataContainer;

import lombok.Data;

@Data
public class DataContainerMergingChangeEventListenerInputCreationContext implements ChangeEventListenerInputCreationContext {
	
	private DataContainer dataContainer;
	
	private String dataSource;

}
