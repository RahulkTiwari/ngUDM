/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InsIdNotFoundFilteredInactivationServiceImpl.java
 * Author:	Padgaonkar S
 * Date:	05-Jan-2022
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.enrichment.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.events.EventMessage;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.domain.DataContainerCloneService;
import com.smartstreamrdu.service.lookup.LookupService;
import com.smartstreamrdu.service.lookup.SecurityFetchService;
import com.smartstreamrdu.service.persist.MergeAndPersistService;
import com.smartstreamrdu.service.status.StatusService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This service is responsible for processing inactivation of filtered
 * container.
 * 
 * Currently this service is applicable for security containers only. Following
 * are the cases when we filtered security from system.
 * 
 * If security status is Inactive & if that security is not available in
 * database .Then we filter that security[As we do not insert inactive security
 * into system] during udl flow.
 * 
 * But there is chances that same security is available in database with another
 * instrument.Hence we need to check if this filtered security is available with
 * another instrument or not..& if yes based on vendor configuration either we
 * inactivate it or we skip it.
 * 
 * UDL sends message of filtered securities to DEN & DEN currently uses this
 * service to proceed inactivation.
 * 
 * @author Padgaonkar
 *
 */
@Component
@Slf4j
public class InsIdNotFoundFilteredInactivationServiceImpl extends AbstractDataContainerInactivationService
		implements InsIdNotFoundFilteredInactivationService {

	@Setter
	@Autowired
	private SecurityFetchService service;

	@Setter
	@Autowired
	private MergeAndPersistService mergeService;

	@Setter
	@Autowired
	private LookupService lookupService;

	@Setter
	@Autowired
	private StatusService statusService;

	@Setter
	@Autowired
	private DataContainerCloneService cloneService;

	@Setter
	@Autowired
	private MergeAndPersistService mergeAndPersist;

	@Override
	public void inactivateContainer(EventMessage eventMessage, DataLevel level) throws UdmBaseException {

		// currently service supports inactivation of filtered security container only
		if (DataLevel.SEC != level) {
			return;
		}

		String dataSource = (String) eventMessage.getEventAttributes().get(SdDataAttrConstant.COL_DATA_SOURCE);
		String secSourceUniqueId = (String) eventMessage.getEventAttributes()
				.get(SecurityAttrConstant.COL_SECURITY_SOURCE_UNIQUE_ID);

		// input filtered security Container
		DataContainer inputSecContainer = getInputSecurity(secSourceUniqueId);
		List<DataContainer> secConList = new ArrayList<>();
		secConList.add(inputSecContainer);

		// retrieving db instrument container for filtered security container.
		List<DataContainer> dbInstrumentContainerForSecurities = lookupService
				.getActiveDbInstrumentContainerForSecurity(new DomainType(dataSource), secConList);

		DataContainer dbInstrumentContainerForSecurity = getDbDataContainer(dbInstrumentContainerForSecurities,
				secSourceUniqueId);

		if (dbInstrumentContainerForSecurity == null) {
			log.debug("Returning as there is no/multiple dataContainers returned for input secSourceUniqueId :{} ",
					secSourceUniqueId);
			return;
		}

		DataContainer minAttributeInsDataContainer = getMinimumAttributeInsDataContainer(
				dbInstrumentContainerForSecurity, new DomainType(dataSource));

		List<DataContainer> allChildDataContainers = dbInstrumentContainerForSecurity.getAllChildDataContainers();

		for (DataContainer con : allChildDataContainers) {

			String sourceUniqueId = con.getHighestPriorityValue(SecurityAttrConstant.SECURITY_SOURCE_UNIQUE_ID);

			if (secSourceUniqueId.equals(sourceUniqueId) && !statusService.isStatusInactive(con, dataSource)) {
				DataContainer minAttSecDataContainer = getMinAttSecDataContainer(con, new DomainType(dataSource));
				minAttributeInsDataContainer.addDataContainer(minAttSecDataContainer, DataLevel.SEC);
			}
		}

		List<DataContainer> conList = new ArrayList<>();
		conList.add(dbInstrumentContainerForSecurity);

		// Invoking persistence layer
		mergeAndPersist.mergeAndPersistSd(dataSource, minAttributeInsDataContainer, conList, null);
	}

	/**
	 * Extracting dbDataContainer from list.
	 * 
	 * @param dbInstrumentContainerForSecurities
	 * @param secSourceUniqueId
	 * @return
	 */
	private DataContainer getDbDataContainer(List<DataContainer> dbInstrumentContainerForSecurities,
			String secSourceUniqueId) {

		if (dbInstrumentContainerForSecurities == null || dbInstrumentContainerForSecurities.isEmpty()) {
			// Retrieving no dataContainer corresponding to secSourceUniqueId is a valid
			// case when
			// vendor sends us inactive security at day1 itself.
			log.info("No data Container is retrieved for input secSourceUniqueId:{}", secSourceUniqueId);
			return null;
		}

		if (dbInstrumentContainerForSecurities.size() > 1) {
			log.error(
					"Multiple data Containers are retrieved for input securitySourceUniqueId : {},hence returning null",
					secSourceUniqueId);
			return null;
		}

		return dbInstrumentContainerForSecurities.get(0);
	}

	/**
	 * creating dummy input security.
	 * 
	 * @param secSourceUniqueId
	 * @return
	 */
	private DataContainer getInputSecurity(String secSourceUniqueId) {
		DataContainer con = new DataContainer(DataLevel.SEC, null);
		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.FEED, secSourceUniqueId);
		con.addAttributeValue(SecurityAttrConstant.SECURITY_SOURCE_UNIQUE_ID, val);
		return con;
	}

}
