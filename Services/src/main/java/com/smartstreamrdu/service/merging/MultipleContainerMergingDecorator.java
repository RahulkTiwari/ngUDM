/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	MultipleContainerMergingDecorator.java
 * Author:	Jay Sangoi
 * Date:	24-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RduLockLevelInfo;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.Constant.CrossRefConstants;
import com.smartstreamrdu.util.LambdaExceptionUtil;

/**
 * @author Jay Sangoi
 *
 */
@Component
@Scope("prototype")
public class MultipleContainerMergingDecorator extends DataContainerMergingDecorator {

	public MultipleContainerMergingDecorator(DataContainerMerging decorator) {
		super(decorator);
	}

	/**
	 * @param decorator
	 */

	private XrfChildContainerComparator childComparator = new XrfChildContainerComparator();
	private XrfLinkComparator linkComparator = new XrfLinkComparator();
	private XrfParentContainerComparator parentComparator = new XrfParentContainerComparator();

	@Override
	public void merge(DataContainer feedContainer, List<DataContainer> dbContainers)
			throws Exception {
		super.merge(feedContainer, dbContainers);
		if (feedContainer.getLevel().equals(DataLevel.XRF_INS)) {
			mergeCrossRefContainer(feedContainer, dbContainers);
		}
	}

	public void mergeCrossRefContainer(DataContainer feedContainer, List<DataContainer> dbContainers) {
		if (CollectionUtils.isEmpty(dbContainers)) {
			return;
		}
		DataContainer merged = parentComparator.compareDataContainer(feedContainer, dbContainers);
		dbContainers.forEach(container -> {
			if (!container.equals(merged)) {
				DataContainer newContainer = (DataContainer) SerializationUtils.clone(container);
				merge(merged, newContainer);
				logicallyDeleteDataContainer(container);
			}
		});
	}

	private void logicallyDeleteDataContainer(DataContainer dataContainer) {
		DataAttribute insStatus = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XR_INS_STATUS,
				DataLevel.XRF_INS);
		DataAttribute secStatus = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XR_SEC_STATUS,
				DataLevel.XRF_SEC);

		DomainType deletedStatus = new DomainType();
		deletedStatus.setNormalizedValue(Constant.DomainStatus.DELETE);

		DataValue<DomainType> statusD = new DataValue<>();
		statusD.setValue(LockLevel.RDU, deletedStatus, new RduLockLevelInfo());

		dataContainer.addAttributeValue(insStatus, statusD);
		populateLinkStatusForContainer(dataContainer, DataLevel.XRF_INS);
		List<DataContainer> childContainers = dataContainer.getChildDataContainers(DataLevel.XRF_SEC);
		if (CollectionUtils.isNotEmpty(childContainers)) {
			childContainers.forEach(secContainer -> {
				secContainer.addAttributeValue(secStatus, statusD);
				populateLinkStatusForContainer(secContainer, DataLevel.XRF_SEC);
			});
		}
	}

	private void merge(DataContainer merged, DataContainer dbContainer) {
		List<DataContainer> mergedChildContainers = merged.getChildDataContainers(DataLevel.XRF_SEC);
		List<DataContainer> dbChildConatiners = dbContainer.getChildDataContainers(DataLevel.XRF_SEC);
		if (CollectionUtils.isEmpty(dbChildConatiners)) {
			return;
		}
		if (CollectionUtils.isEmpty(mergedChildContainers)) {
			merged.addDataContainers(dbChildConatiners, DataLevel.XRF_SEC);
			return;
		}
		dbChildConatiners.forEach(LambdaExceptionUtil.rethrowConsumer(x -> {
			if (!checkChildConatainerEqual(x, mergedChildContainers)) {
				merged.addDataContainer(x, DataLevel.XRF_SEC);
			}
		}));
	}

	private boolean checkChildConatainerEqual(DataContainer dbChildContainer,
			List<DataContainer> mergedChildContainers) {
		for (DataContainer mergeChildContainer : mergedChildContainers) {
			if (childComparator.compare(mergeChildContainer, dbChildContainer)) {
				populateStartDateForLinks(mergeChildContainer, dbChildContainer);
				return true;
			}
		}
		return false;
	}

	private void populateStartDateForLinks(DataContainer mergedChildContainer, DataContainer dbChildContainer) {
		DataAttribute linkAttribute = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEC_LINKS,
				DataLevel.XRF_SEC);
		ArrayList<DataRow> mergedRows = getLinkDataRows(mergedChildContainer, linkAttribute);
		ArrayList<DataRow> dbRows = getLinkDataRows(dbChildContainer, linkAttribute);
		if (mergedRows == null || dbRows == null || CollectionUtils.isEmpty(mergedRows) || CollectionUtils.isEmpty(dbRows)) {
			return;
		}
		mergedRows.forEach(mergedRow -> dbRows.forEach(dbRow -> {
			if (linkComparator.compareLinks(mergedRow, dbRow, DataLevel.XRF_SEC)) {
				setStartDateForRow(mergedRow);
			}
		}));

	}

	private void setStartDateForRow(DataRow secRow) {
		DataAttribute startDateA = DataAttributeFactory
				.getAttributeByNameAndLevel(CrossRefConstants.SEC_LINK_INITIAL_DATE, DataLevel.XRF_SEC);
		DataValue<LocalDateTime> startDate = new DataValue<>();
		startDate.setValue(LockLevel.RDU, LocalDateTime.now(), new RduLockLevelInfo());
		secRow.addAttribute(startDateA, startDate);
	}

	private void populateLinkStatusForContainer(DataContainer container, DataLevel level) {
		DataAttribute linkAttribute = null;

		if (level.equals(DataLevel.XRF_INS)) {
			linkAttribute = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINKS,
					DataLevel.XRF_INS);
		} else {
			linkAttribute = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEC_LINKS,
					DataLevel.XRF_SEC);
		}
		ArrayList<DataRow> allLinks = getLinkDataRows(container, linkAttribute);
		if (null == allLinks || CollectionUtils.isEmpty(allLinks)) {
			return;
		}
		allLinks.forEach(x -> setEndDateAndStatus(x, level));
	}

	private void setEndDateAndStatus(DataRow row, DataLevel level) {
		DataAttribute endDateAttr = null;
		DataAttribute linkStatusAttr = null;
		if (level.equals(DataLevel.XRF_INS)) {
			endDateAttr = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINK_END_DATE, level);
			linkStatusAttr = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XR_INS_LINK_STATUS,
					level);
		} else {
			endDateAttr = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.SEC_LINK_END_DATE, level);
			linkStatusAttr = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.XR_SEC_LINK_STATUS,
					level);
		}
		DataValue<LocalDateTime> endDate = new DataValue<>();
		endDate.setValue(LockLevel.RDU, LocalDateTime.now(), new RduLockLevelInfo());
		DataValue<Boolean> status = new DataValue<>();
		status.setValue(LockLevel.RDU, false, new RduLockLevelInfo());

		row.addAttribute(endDateAttr, endDate);
		row.addAttribute(linkStatusAttr, status);
	}

	@SuppressWarnings("unchecked")
	private ArrayList<DataRow> getLinkDataRows(DataContainer container, DataAttribute linkAttribute) {
		Serializable allLinks = container.getAttributeValue(linkAttribute);
		ArrayList<DataRow> linksList = null;
		if (allLinks != null && allLinks instanceof DataRow) {
			DataRow row = (DataRow) allLinks;
			if (row.getValue() != null && row.getValue().getValue(LockLevel.RDU) != null
					&& row.getValue().getValue(LockLevel.RDU) instanceof ArrayList) {
				linksList = (ArrayList<DataRow>) row.getValue().getValue(LockLevel.RDU);
			}
		}
		return linksList;
	}
}
