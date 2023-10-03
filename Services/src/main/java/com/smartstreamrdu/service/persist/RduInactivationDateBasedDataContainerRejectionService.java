/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    RduInactivationDateBasedDataContainerRejectionService.java
 * Author:  Padgaonkar
 * Date:    Feb 10, 2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.persist;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import org.springframework.stereotype.Component;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to validate dataContainer based on rduInactivationDate. If
 * rduInactivationDate is less than current date then its removes dataContainer
 * from list & hence restrict it from persistence. We need to apply this logic
 * only in case of new inserts of dataContainer.
 * 
 * @author Padgaonkar
 *
 */
@Component
@Slf4j
public class RduInactivationDateBasedDataContainerRejectionService implements DataContainerRejectionService {

	@Override
	public void validateAndRemoveInvalidContainerFromList(List<DataContainer> dataContainers) {

		Iterator<DataContainer> iterator = dataContainers.iterator();
		while (iterator.hasNext()) {
			DataContainer container = iterator.next();

			if (isContainerApplicable(container)) {
				LocalDate highestPriorityValue = container
						.getHighestPriorityValue(SdDataAttributeConstant.RDU_INACTIVATION);

				if (highestPriorityValue != null && highestPriorityValue.isBefore(LocalDate.now())) {
					log.info("Following dataContainer:{} is removed from persistenceList as rduInacticationDate is :{} before currentDate:{}",
							container,highestPriorityValue, LocalDate.now());
					iterator.remove();
				}
			}
		}

	}

	/**
	 * This method validates whether container is applicalbe or not.
	 * 
	 * @param container
	 * @return
	 */
	private boolean isContainerApplicable(DataContainer container) {
		return container.isNew() && container.getLevel() != null && container.getLevel().equals(DataLevel.INS);

	}
}
