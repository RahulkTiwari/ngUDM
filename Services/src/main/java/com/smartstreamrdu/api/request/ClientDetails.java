package com.smartstreamrdu.api.request;

import lombok.Data;

/**
 * @author SPadgaonakar
 * Client Details parameter for getData Service..
 */
@Data
public class ClientDetails {

	private String clientId;

	public String getClientId() {
		return this.clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
}
