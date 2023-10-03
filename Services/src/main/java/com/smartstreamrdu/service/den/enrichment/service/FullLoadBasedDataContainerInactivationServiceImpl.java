/*******************************************************************
*
* Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
* All rights reserved. 
*
* File:	FullLoadBasedDataContainerInactivationServiceImpl.java
* Author:	Padgaonkar S
* Date:	26-Oct-2021
*
*******************************************************************
*/
package com.smartstreamrdu.service.den.enrichment.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.persistence.domain.autoconstants.SdDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.domain.DataContainerCloneService;
import com.smartstreamrdu.service.inactive.InstrumentInactiveUtil;
import com.smartstreamrdu.service.persist.MergeAndPersistService;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdDataAttConstant;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This service is responsible for inactivating instrument/security based on
 * full file load.
 * 
 * @author Padgaonkar
 *
 */
@Component
@Slf4j
public class FullLoadBasedDataContainerInactivationServiceImpl
extends AbstractDataContainerInactivationService implements FullLoadBasedDataContainerInactivationService {

	@Autowired
	@Setter
	private DataContainerCloneService cloneService;

	@Autowired
	@Setter
	private MergeAndPersistService mergeAndPersist;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inactivateApplicableSecurity(Iterator<DataContainer> dataContainerIterator,
			LocalDateTime fullLoadStartDate) throws UdmBaseException {

		while (dataContainerIterator.hasNext()) {
			DataContainer sdDataContainer = dataContainerIterator.next();
			sdDataContainer.setNew(false);

			DomainType dataSource = sdDataContainer.getHighestPriorityValue(SdDataAttrConstant.DATA_SOURCE);

			// creating minimum attribute instrument dataContainer
			DataContainer minimumAttributeInsDataContainer = getMinimumAttributeInsDataContainer(sdDataContainer,
					dataSource);

			List<DataContainer> allChildDataContainers = sdDataContainer.getAllChildDataContainers();
			for (DataContainer sec : allChildDataContainers) {

				LocalDateTime lastProcessedDate = sec
						.getHighestPriorityValue(SecurityAttrConstant.SEC_LAST_PROCESSED_DATE);

				// if lastProcessed is before fullLoadStartDate && if security is active
				// -- we inactivate it
				if (lastProcessedDate.isBefore(fullLoadStartDate)
						&& !InstrumentInactiveUtil.isSecurityInactive(sec, dataSource.getVal())) {
					DataContainer minAttSecDataContainer = getMinAttSecDataContainer(sec, dataSource);
					addSecurityIdAndType(minAttSecDataContainer, sec);
					minimumAttributeInsDataContainer.addDataContainer(minAttSecDataContainer, DataLevel.SEC);
				}

			}
			List<DataContainer> conList = new ArrayList<>();
			conList.add(sdDataContainer);

			// Based on security state determine instrument status.
			// If instrument contains a technical security & if that security is getting
			// inactivated
			// In that case we need to inactivate instrument also.
			calculateInstrumentStatus(minimumAttributeInsDataContainer);

			// Processing inactivation
			log.info("processing dataContainer:{} for inactivation based on full load",minimumAttributeInsDataContainer);
			mergeAndPersist.mergeAndPersistSd(dataSource.getVal(), minimumAttributeInsDataContainer, conList, null);
		}

	}

	/**
	 * This attribute is specifically needed for technical security during
	 * merge.Here merge is happen based on seurityId instead securitySourceUniqueId.
	 */
	private void addSecurityIdAndType(DataContainer minAttSecDataContainer, DataContainer sec) {

		String securityId = sec.getHighestPriorityValue(SecurityAttrConstant._SECURITY_ID);
		DataValue<String> idValue = new DataValue<>();
		idValue.setValue(LockLevel.FEED, securityId);
		minAttSecDataContainer.addAttributeValue(SecurityAttrConstant._SECURITY_ID, idValue);

		DataValue<DomainType> val = getSecurityTypeVal(sec);
		
		minAttSecDataContainer.addAttributeValue(SecurityAttrConstant.RDU_SECURITY_TYPE, val);

	}

	/**
	 * Populating securityType
	 * @param sec
	 * @return
	 */
	private DataValue<DomainType> getSecurityTypeVal(DataContainer sec) {
		DomainType securityType = sec.getHighestPriorityValue(SecurityAttrConstant.RDU_SECURITY_TYPE);
		DataValue<DomainType> val = new DataValue<>();
		
		if(SdDataAttConstant.TECHNICAL.equals(securityType.getNormalizedValue())) {
			val.setValue(LockLevel.ENRICHED, securityType);
		}else {
			val.setValue(LockLevel.FEED, securityType);
		}
		return val;
	}


	/**
	 * This method determines instrument status based on security dataContainers.
	 * 
	 * @param minimumAttributeInsDataContainer
	 */
	private void calculateInstrumentStatus(DataContainer minimumAttributeInsDataContainer) {
		List<DataContainer> allChildDataContainers = minimumAttributeInsDataContainer.getAllChildDataContainers();

		if (!allChildDataContainers.isEmpty() && allChildDataContainers.size() == 1
				&& isTechnicalSecurity(allChildDataContainers.get(0))) {
			DataValue<DomainType> insStatus = new DataValue<>();
			insStatus.setValue(LockLevel.ENRICHED, new DomainType(null, null, DomainStatus.INACTIVE));
			minimumAttributeInsDataContainer.addAttributeValue(SdDataAttributeConstant.INS_STATUS, insStatus);
		}

	}

	/**
	 * Validating is input security is technical security or not
	 * 
	 * @param dataContainer
	 * @return
	 */
	private boolean isTechnicalSecurity(DataContainer dataContainer) {
		DomainType securityType = (DomainType) dataContainer
				.getHighestPriorityValue(SecurityAttrConstant.RDU_SECURITY_TYPE);
		return securityType != null && SdDataAttConstant.TECHNICAL.equals(securityType.getNormalizedValue());
	}

}
