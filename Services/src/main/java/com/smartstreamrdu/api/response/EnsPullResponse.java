/*******************************************************************
*
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	EnsPullResponse.java
* Author:	Ravi Kaithwas
* Date:	18-Aug-2021
*
*******************************************************************
*/
package com.smartstreamrdu.api.response;

import java.util.LinkedHashMap;

import com.smartstreamrdu.persistence.domain.ProformaEnData;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * response object for ens search api.
 * <br> * store ens proforma data into json based hash map structure.
 *  
 * @author RKaithwas
 *
 */
@ArraySchema(schema = @Schema(implementation = ProformaEnData.class))
public class EnsPullResponse extends LinkedHashMap<String, Object> implements Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9200472556701879032L;

}
