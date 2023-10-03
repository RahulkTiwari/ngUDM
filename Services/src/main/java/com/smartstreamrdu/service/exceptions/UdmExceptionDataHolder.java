/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdmExceptionData.java
 * Author:	Rushikesh Dedhia
 * Date:	25-Feb-2019
 *
 *******************************************************************/
package com.smartstreamrdu.service.exceptions;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *  This is a POJO class that will hold all the information required throughout the life cycle of a 
 *  exception in the process.
 * @author Dedhia
 *
 */
public class UdmExceptionDataHolder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2290184595976761062L;
	
	private String id;
	
	private String exceptionType;
	
	private String incidentId;
	
	private String category;//10042
	private String subCategory;//10040
	private String description;
	private String priority;
	private String stormCriteriaValue;
	private String summary;
	private String component;
	private String status;
	
	private String updateBy;
	// 	
	private Map<String,String> customFieldValues;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("exceptionType", exceptionType).append("incidentId", incidentId).append("_id", id)
				.append("category", category).append("subCategory", subCategory).append("description", description)
				.append("priority", priority).append("stormCriteriaValue", stormCriteriaValue)
				.append("summary", summary).append("component", component).append("customFieldValues", customFieldValues).
				append("status", status).toString();
	}


	private UdmExceptionDataHolder(Builder builder) {
		this.exceptionType = builder.exceptionType;
		this.incidentId = builder.incidentId;
		this.category = builder.category;
		this.subCategory = builder.subCategory;
		this.description = builder.description;
		this.priority = builder.priority;
		this.stormCriteriaValue = builder.stormCriteriaValue;
		this.summary = builder.summary;
		this.component = builder.component;
		this.customFieldValues = builder.customFieldValues;
		this.status = builder.status;
		this.id = builder.id;
		this.updateBy = builder.updateBy;
	}


	public String getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}

	public String getIncidentId() {
		return incidentId;
	}

	public void setIncidentId(String incidentId) {
		this.incidentId = incidentId;
	}

	public String getCategory() {
		return category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public String getDescription() {
		return description;
	}

	public String getPriority() {
		return priority;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getId() {
		return id;
	}

	public String getStormCriteriaValue() {
		return this.stormCriteriaValue;
	}
	
	public String getSummary() {
		return summary;
	}

	public String getComponent() {
		return component;
	}
	
	public String getUpdateBy() {
		return updateBy;
	}

	/**
	 * Creates builder to build {@link UdmExceptionDataHolder}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link UdmExceptionDataHolder}.
	 */
	public static final class Builder {
		private String exceptionType;
		private String incidentId;
		private String category;
		private String subCategory;
		private String description;
		private String priority;
		private String stormCriteriaValue;
		private String summary;
		private String component;
		private Map<String,String> customFieldValues;
		private String status;
		private String id;
		private String updateBy;
		

		private Builder() {
		}

		public Builder withExceptionType(String exceptionType) {
			this.exceptionType = exceptionType;
			return this;
		}

		public Builder withIncidentId(String incidentId) {
			this.incidentId = incidentId;
			return this;
		}

		public Builder withCategory(String category) {
			this.category = category;
			return this;
		}

		public Builder withSubCategory(String subCategory) {
			this.subCategory = subCategory;
			return this;
		}

		public Builder withDescription(String description) {
			this.description = description;
			return this;
		}
		
		public Builder withSummary(String summary) {
			this.summary = summary;
			return this;
		}
		
		public Builder withComponent(String component) {
			this.component = component;
			return this;
		}
		
		public Builder withPriority(String priority) {
			this.priority = priority;
			return this;
		}

		
		public Builder withStormCriteriaValue(String stormCriteriaValue) {
			this.stormCriteriaValue = stormCriteriaValue;
			return this;
		}
		
		public Builder withCustomFieldValues(Map<String, String> customFieldValues) {
			this.customFieldValues = customFieldValues;
			return this;
		}
		
		public Builder withStatus(String status) {
			this.status = status;
			return this;
		}
		
		public Builder withId(String id) {
			this.id = id;
			return this;
		}
		
		public Builder withUpdateBy(String updateBy) {
			this.updateBy = updateBy;
			return this;
		}
		
		public UdmExceptionDataHolder build() {
			return new UdmExceptionDataHolder(this);
		}
		
	}

	public Map<String,String> getCustomFieldValues() {
		return customFieldValues;
	}


	public void setCustomFieldValues(Map<String,String> customFieldValues) {
		this.customFieldValues = customFieldValues;
	}
	
}
