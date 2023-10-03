/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ApiEvents.java
 * Author: Rushikesh Dedhia
 * Date: Aug 31, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.validation.events;

/**
 * @author Dedhia
 *
 */
public enum ApiEvents {
	
	SYSTEM_ERROR("E1000", "System error"),
	MISSING_ISIN("E1001", "ISIN not specified"),
	INVALID_ISIN("E1002", "Invalid ISIN specified"),
	MISSING_CLIENT("E1009", "Client id not specified"),
	INVALID_MIC("E6002", "Invalid MIC specified"),
	MISSING_MIC("E6006", "MIC not specified"),
	NO_RECORDS_FOUND("W6002", "Record Not Found"),
	SUCCESS("S0000", "Success");

	private String statusCode;
	private String statusMessage;

	ApiEvents(String statusCode, String statusMessge) {
		this.statusCode = statusCode;
		this.statusMessage = statusMessge;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public static String getStatusMessageCode(String code) {
		if (code == null)
			return null;

		ApiEvents event = getEventByCode(code);
		if (event != null)
			return event.getStatusMessage();

		return null;
	}

	public static ApiEvents getEventByCode(String code) {
		ApiEvents[] events = values();
		for (ApiEvents event : events) {
			if (event.getStatusCode().equalsIgnoreCase(code)) {
				return event;
			}
		}
		return null;
	}
}
