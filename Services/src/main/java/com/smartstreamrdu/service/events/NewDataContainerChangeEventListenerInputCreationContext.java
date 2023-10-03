package com.smartstreamrdu.service.events;

import com.smartstreamrdu.domain.DataContainer;

import lombok.Data;

@Data
public class NewDataContainerChangeEventListenerInputCreationContext implements ChangeEventListenerInputCreationContext {
	
	private DataContainer dataContainer;
	

}
