/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	ProfileProcessingRequest.java
 * Author:	RKaithwas
 * Date:	28-Apr-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.api.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * request object for profile processing api call
 * @author RKaithwas
 *
 */
@Data
public class ProfileProcessingRequest implements Request{
	/**
	 * 
	 */
	private static final long serialVersionUID = 416528659279400L;
	
	//2020-03-23T00:00:00.000Z
	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-ddThh:mm:ss.SSSZ")
	private LocalDateTime fromDateTime; 
}
