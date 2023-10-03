/*******************************************************************
*
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	EnsPullRequestByDate.java
* Author:	Ravi Kaithwas
* Date:	18-Aug-2021
*
*******************************************************************
*/
package com.smartstreamrdu.api.request;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * request object for ens search api 
 * @author RKaithwas
 *
 */
@Data
public class EnsPullRequestByDate implements Request{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2240135489524270304L;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate fromEventEffectiveDate;
	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate toEventEffectiveDate;

}
