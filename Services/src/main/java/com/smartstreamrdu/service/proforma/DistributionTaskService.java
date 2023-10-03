/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DistributionTaskService.java
 * Author : SaJadhav
 * Date : 05-Nov-2019
 * 
 */
package com.smartstreamrdu.service.proforma;

import com.smartstreamrdu.persistence.domain.ProformaDistributionTask;

/**
 * Service for Distribution Task queries
 * 
 * @author SaJadhav
 *
 */
public interface DistributionTaskService {

	/**
	 * gets Distribution Task details from task name Note:This same interface will
	 * be used to get task details after ProfileAndSubscription Implementation.
	 * 
	 * @param taskName
	 * @return
	 */
	public ProformaDistributionTask getDistributionTaskFromNameAndStatus(String taskName, String status);

	/**
	 * Returns delta Distribution task Name for the profile name
	 * 
	 * @param profileName
	 * @return
	 */
	public String getDeltaDistributionTaskNameForProfileName(String profileName);

	/**
	 * This function will find active distribution task for profile name and update
	 * it with status as inactive and persist.
	 * 
	 * @param profileName
	 * @param user
	 */
	public void inactivateDisTasksForProfileName(String profileName, String user);

}
