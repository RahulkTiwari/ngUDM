/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DistributionTaskServiceImpl.java
 * Author : SaJadhav
 * Date : 05-Nov-2019
 * 
 */
package com.smartstreamrdu.service.proforma;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.proforma.DistributionFileType;
import com.smartstreamrdu.persistence.domain.ProformaDistributionTask;
import com.smartstreamrdu.persistence.repository.ProformaDistributionTaskRepository;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * Service for Distribution Task queries
 * 
 * @author SaJadhav
 *
 */
@Component
public class DistributionTaskServiceImpl implements DistributionTaskService {

	@Autowired
	private ProformaDistributionTaskRepository disTaskRepo;

	/**
	 * Gets Distribution Task details from task name. This is a temporary
	 * implementation to get distribution task details from system property. Once
	 * Profile-subscription is implemented this implementation will change.
	 * 
	 * @param taskName
	 * @return
	 */
	public ProformaDistributionTask getDistributionTaskFromNameAndStatus(String taskName, String status) {

		return disTaskRepo.findDistributionTaskByTaskNameAndStatus(taskName, status);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDeltaDistributionTaskNameForProfileName(String profileName) {
		ProformaDistributionTask findDisTaskByProfileNameAndFileType = disTaskRepo
				.findDisTaskByProfileNameAndFileType(profileName, DistributionFileType.DELTA);
		if (findDisTaskByProfileNameAndFileType != null) {
			return findDisTaskByProfileNameAndFileType.getName();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inactivateDisTasksForProfileName(String profileName, String user) {
		List<ProformaDistributionTask> distributionTasks = disTaskRepo.findDisTaskByProfileNameAndStatus(profileName,
				DomainStatus.ACTIVE);
		distributionTasks.stream().forEach(disTask -> updateDisTask(disTask, user));

		disTaskRepo.saveAll(distributionTasks);
	}

	/**
	 * This function set distribution task status to inactive. And also populate
	 * update user and date
	 * 
	 * @param distributionTasks
	 * @param user
	 * @return
	 */
	private ProformaDistributionTask updateDisTask(ProformaDistributionTask distributionTasks, String user) {
		distributionTasks.setStatus(DomainStatus.INACTIVE);
		distributionTasks.setUpdUser(user);
		distributionTasks.setUpdDate(LocalDateTime.now());
		return distributionTasks;
	}
}
