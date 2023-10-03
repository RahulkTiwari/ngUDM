/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DefaultMessage.java
 * Author:	Jay Sangoi
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.messaging;

import java.io.Serializable;
import java.util.Objects;

import com.smartstreamrdu.domain.message.UdmMessageKey;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.Component;
import com.smartstreamrdu.util.Constant.Process;

import lombok.Data;

/**
 * @author Jay Sangoi
 *
 */
@Data
public class DefaultMessage implements Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String data;
	
	private String key;
	
	private Component source;
	
	private Component target;
	
	private Process process;
	
	private Integer partition;
	
	private DefaultMessage(Builder builder){
		Objects.requireNonNull(builder);
		Objects.requireNonNull(builder.data,Constant.NOT_NULL_MESSAGE("Data", "Message"));
		Objects.requireNonNull(builder.target,Constant.NOT_NULL_MESSAGE("Target", "Message"));
		//TODO Convert data to string using json
		this.data = String.valueOf(builder.data);
		//If key == null then above conversion returns value as "null".which 
		//results into sending all messages in single partition.hence changing 
		//this to return null.
		this.key = builder.key!=null ? JsonConverterUtil.convertToJson(builder.key):null;
		this.source = builder.source;
		this.target = builder.target;
		this.process = builder.process;
		this.partition=builder.partition;
	}
	
	
	public static class Builder {
		private Serializable data;
		
		private UdmMessageKey key;
		
		private Component source;
		
		private Component target;
		
		private Process process;
		
		private Integer partition;
		
		public Builder data(Serializable data){
			this.data = data;
			return this;
		}
		
		public Builder key(UdmMessageKey key){
			this.key = key;
			return this;
		}

		public Builder source(Component source){
			this.source = source;
			return this;
		}
		
		public Builder target(Component target){
			this.target = target;
			return this;
		}	
		
		public Builder process(Process process){
			this.process = process;
			return this;
		}
		
		public Builder partition(Integer partition){
			this.partition = partition;
			return this;
		}
		
		public Message build(){
			String json=JsonConverterUtil.convertToJson(this.data);
			this.data=json;
			return new DefaultMessage(this);
			
		}
		
	}


}
