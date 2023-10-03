package com.smartstreamrdu.api.request;

import java.io.Serializable;
import java.util.Date;

import com.smartstreamrdu.persistence.retrival.Criteria;

import lombok.Data;

/**
 * @author SPadgaonakar
 * Wrapper class which send input to getData() webService 
 */
@Data
public class GetDataServiceInputParameter implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1478434061043402900L;

	private ClientDetails clientDetails;
	
	private Criteria requestCriteria;
	
	private String taskName;
	
	private Date fromDate;
	
	private String profileName;
	
	private Boolean deltaDistribution;
	
	public Boolean getDeltaDistribution() {
		return deltaDistribution;
	}
	public void setDeltaDistribution(Boolean deltaDistribution) {
		this.deltaDistribution = deltaDistribution;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public ClientDetails getClientDetails() {
		return clientDetails;
	}
	public Criteria getRequestCriteria() {
		return requestCriteria;
	}

	public void setRequestCriteria(Criteria requestCriteria) {
		this.requestCriteria = requestCriteria;
	}

	public void setClientDetails(ClientDetails clientDetails) {
		this.clientDetails = clientDetails;
	}
	public Date getFromDate() {
		return fromDate;
	}
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	
	/**
	 * @return the profileName
	 */
	public String getProfileName() {
		return profileName;
	}
	/**
	 * @param profileName the profileName to set
	 */
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public boolean isDeltaDistribution() {
		
		boolean isDelta = false;
		
		if (fromDate != null) {
			isDelta = true;
		}
		
		return isDelta;
	}
	
}
