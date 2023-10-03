/**
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: ApiResponse.java
 * Author : SaJadhav
 * Date : 27-Aug-2018
 * 
 */
package com.smartstreamrdu.api.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.smartstreamrdu.persistence.domain.ProformaInstrument;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author SaJadhav
 *
 */
@ToString
@EqualsAndHashCode
public class ApiResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Set<String> responseCode;
	private Set<String> responseString;
	private List<ResponseContent> content;

	public ApiResponse() {
		this.responseCode = new LinkedHashSet<>(5);
		this.responseString = new LinkedHashSet<>(5);
	}

	public ApiResponse(Set<String> responseCode, Set<String> responseString, List<ResponseContent> content) {
		this.responseCode = responseCode;
		this.responseString = responseString;
		this.content = content;
	}


	public void addContent(ResponseContent content) {
		if(this.content == null){
			this.content=new ArrayList<>();
		}
		this.content.add(content);
	}
	
	public void addResponseCodeNMessage(String responseCode,String responseMessage){
		this.responseCode.add(responseCode);
		this.responseString.add(responseMessage);
	}

	public Set<String> getResponseCode() {
		return responseCode;
	}

	public Set<String> getResponseString() {
		return responseString;
	}

	@Schema(implementation = ProformaInstrument.class)
	public List<ResponseContent> getContent() {
		return content;
	}
	

}
