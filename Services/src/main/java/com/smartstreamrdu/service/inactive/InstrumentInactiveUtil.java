/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	InstrumentInactiveUtil.java
 * Author:	Jay Sangoi
 * Date:	08-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBusinessException;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.domain.autoconstants.InstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.extern.slf4j.Slf4j;
/**
 * @author Jay Sangoi
 * NOTE:This class provides services which is specific to sdData only.
 */
@Slf4j
public class InstrumentInactiveUtil {
	
	/**
	 * 
	 */

	private InstrumentInactiveUtil(){
		
	}
	
	public static void inactiveInstrumentIfAllSecurituesInactive(String datasource, DataContainer container) throws UdmBusinessException, UdmTechnicalException {
		if(container == null || DataLevel.INS != container.getLevel() || datasource == null ){
			//either the container is null or data level is not INS, hence returning
			return;
		}
		
		checkFeedStatusValues(datasource, container);
		checkRduStatusValues(datasource,container);
	}

	/**
	 * @param datasource 
	 * @param datasource
	 * @param container
	 * @throws UdmBusinessException 
	 */
	private static void checkRduStatusValues(String datasource, DataContainer container) throws UdmBusinessException {
		DomainType insStatusDv =  (DomainType) container.getAttributeValueAtLevel(LockLevel.RDU, InstrumentAttrConstant.INSTRUMENT_STATUS);
		if (insStatusDv != null && DomainStatus.INACTIVE.equals(insStatusDv.getNormalizedValue())) {
			setChildContainersInactive(container); // Instrument Status inactive. Setting all the securities inactive
			return;
		}
		
		//Check if all the securities are inactive, if yes, set instrument inactive
		List<DataContainer> securityContainers = container.getChildDataContainers(DataLevel.SEC);
		
		if (areAllSecuritiesInactive(securityContainers,datasource)) {
			setInstrumentStatusInactive(container,datasource); // all the securities are inactive. set the instrument inactive
		}
		
	}

	@SuppressWarnings("unchecked")
	private static void setInstrumentStatusInactive(DataContainer container, String datasource) throws UdmBusinessException {
		if(isInstrumentInactive(datasource, container)){
			return;
		}
		DomainType insStatusDomainType;
		insStatusDomainType = new DomainType(null, null, DomainStatus.INACTIVE);
		DataValue<DomainType> insStatusDataValue = (DataValue<DomainType>) container
				.getAttributeValue(InstrumentAttrConstant.INSTRUMENT_STATUS);
		insStatusDataValue.setValue(LockLevel.RDU, insStatusDomainType);

	}

	private static boolean areAllSecuritiesInactive(List<DataContainer> securityContainers, String datasource) {
		if (CollectionUtils.isNotEmpty(securityContainers)) {
			return securityContainers.stream().allMatch(securityContainer -> isSecurityInactive(securityContainer,datasource));
		} else {
			return false;
		}
	}
	
	public static boolean isSecurityInactive(DataContainer securityDataContainer, String datasource) {
		DomainType domainType=securityDataContainer.getHighestPriorityValue(SecurityAttrConstant.SECURITY_STATUS);
		String normalizedValue=domainType.getNormalizedValue();
		if(StringUtils.isEmpty(normalizedValue)){
			NormalizedValueService normalizedService = SpringUtil.getBean(NormalizedValueService.class);
			normalizedValue=(String) normalizedService.getNormalizedValueForDomainValue(SecurityAttrConstant.SECURITY_STATUS, domainType, datasource);
		}
		return DomainStatus.INACTIVE.equals(normalizedValue);
	}

	/**
	 * set all the security status as inactive because instrument is inactive
	 * @param container
	 */
	private static void setChildContainersInactive(DataContainer container) {
		List<DataContainer> childContainers = container.getChildDataContainers(DataLevel.SEC);
		if (CollectionUtils.isNotEmpty(childContainers)) {
			childContainers.forEach(InstrumentInactiveUtil::setSecurityStatusInactive);
		}
	}

	/**
	 * @param childContainer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static void setSecurityStatusInactive(DataContainer childContainer) {
		DomainType domainType = new DomainType(null, null, DomainStatus.INACTIVE);
		DataValue<Serializable> secStatusDataValue = (DataValue<Serializable>) childContainer.getAttributeValue(SecurityAttrConstant.SECURITY_STATUS);
		secStatusDataValue.setValue(LockLevel.RDU, domainType);
	}

	@SuppressWarnings("unchecked")
	private static void checkFeedStatusValues(String datasource, DataContainer container) throws UdmBusinessException, UdmTechnicalException {
		
		if(isInstrumentInactive(datasource, container)){
			//Instrument status inactive, hence returning
			return;
		}
		
		List<DataContainer> childDataContainers = container.getChildDataContainers(DataLevel.SEC);
		if(CollectionUtils.isEmpty(childDataContainers)){
			//No child container, hence returning
			return;
		}
		
		//Iterate over all the child and check if any of the status is active
		boolean flag = false;
		
		String inactiveValue = null;

		for (DataContainer dataContainer : childDataContainers) {

			Serializable attributeValue = dataContainer.getAttributeValue(SecurityAttrConstant.SECURITY_STATUS);
			if (!(attributeValue instanceof DataValue)) {
				// Security status is not populated hence returning it
				flag = true;
				break;
			}
			DataValue<DomainType> securityStatus = (DataValue<DomainType>) attributeValue;

			if (securityStatus.getValue(LockLevel.FEED) == null
					|| securityStatus.getValue(LockLevel.FEED).getVal() == null) {
				// Security status is not populated hence returning it
				flag = true;
				break;
			}
			
			NormalizedValueService normalizedService = SpringUtil.getBean(NormalizedValueService.class);
			// Get the normalized value for status
			flag = DomainStatus.ACTIVE.equals(normalizedService.getNormalizedValueForDomainValue(
					SecurityAttrConstant.SECURITY_STATUS, securityStatus.getValue(LockLevel.FEED), datasource));
			if (flag) {
				break;
			}

			inactiveValue = securityStatus.getValue().getVal();

		}
		
		if(!flag && inactiveValue != null){
			// All securities are inactive 
			DataValue<DomainType> dv = new DataValue<>();
			String rduDomain = DataAttributeFactory.getRduDomainForDomainDataAttribute(InstrumentAttrConstant.INSTRUMENT_STATUS);
			String domainName = getDomainName(datasource, rduDomain);
			DomainType ty = new DomainType(inactiveValue, null, null,domainName);
			dv.setValue(LockLevel.FEED, ty);
			container.addAttributeValue(InstrumentAttrConstant.INSTRUMENT_STATUS, dv);
			//Audit should be added on the instrument level when instrument is getting inactivated.
			container.addAudit();
		}
	}

	/**
	 * This method return domainName for given input dataSource and rduDomain.
	 * eg.in case of esma inActivation rduDomain is statuses but domainname is
	 * instrumentStatusMap whereas in case of trdse rduDoamin is still same but 
	 * domainName is tradingStatusMap.
	 * @param datasource
	 * @param rduDomain
	 * @return
	 * @throws UdmTechnicalException
	 */
	private static String getDomainName(String datasource, String rduDomain) throws UdmTechnicalException {
		CacheDataRetrieval cacheDataRetrieval = SpringUtil.getBean(CacheDataRetrieval.class);
		String normalizedValue = DomainStatus.INACTIVE;
		String dataSourceDomainSourceFromCode = cacheDataRetrieval.getDataSourceDomainSourceFromCode(datasource);
		List<DomainType> domainValues = cacheDataRetrieval.getVendorDomainValuesFromCache(rduDomain, dataSourceDomainSourceFromCode,normalizedValue);
		if (domainValues.isEmpty()) {
			throw new UdmTechnicalException(
					"No vendor domain  returned for this feed for inActive statuses : {}" + datasource, null) ;
		}
		if (domainValues.size() > 1) {
			log.debug("Multiple vendor domain  are retrieved for normalized status inactive for feed : {}", datasource);
		}
		return domainValues.get(0).getDomain();
	}
	
	private static boolean isInstrumentInactive(String datasource, DataContainer container) throws UdmBusinessException {
		NormalizedValueService normalizedService = SpringUtil.getBean(NormalizedValueService.class);
		DomainType insStatusDV = (DomainType) container.getAttributeValueAtLevel(LockLevel.FEED,
				InstrumentAttrConstant.INSTRUMENT_STATUS);

		Serializable instrumentStatus = getFeedInstrumentStatus(normalizedService, datasource,
				InstrumentAttrConstant.INSTRUMENT_STATUS, insStatusDV);
		return DomainStatus.INACTIVE.equals(instrumentStatus);
	} 

	/**
	 * 
	 * @param normalizedService
	 * @param datasource
	 * @param insStatusAtt
	 * @param insStatusDV
	 * @return
	 * @throws UdmBusinessException
	 */
	private static Serializable getFeedInstrumentStatus(NormalizedValueService normalizedService, String datasource,
			DataAttribute insStatusAtt, DomainType insStatusDV) throws UdmBusinessException {
		if (insStatusDV != null && !insStatusAtt.isNull(insStatusDV.getVal())) {
			// Get the normalized value
			return normalizedService.getNormalizedValueForDomainValue(insStatusAtt, insStatusDV, datasource);
		}
		return null;
	}
	
	/**
	 * This method will check whether requested instrument is InActive or not.
	 * If yes this method returns true else returns false
	 * @param dataSource
	 * @param container
	 * @return
	 */
	public static boolean isInstrumentStatusInActive(String dataSource, DataContainer container) {
		   DomainType insStatusValue = (DomainType) container.getHighestPriorityValue(SdDataAttributeConstant.INS_STATUS);
			
			if (insStatusValue == null) {
				log.error("no status attribute value found for instrument dataContainer : {}", container);
				return false;
			}
			
			if (StringUtils.isNotEmpty(insStatusValue.getNormalizedValue())
					&& insStatusValue.getNormalizedValue().equals(DomainStatus.INACTIVE)) {
				return true;
			}
			NormalizedValueService normalizedService = SpringUtil.getBean(NormalizedValueService.class);
			Serializable normalizedValueForDomainValue = normalizedService
					.getNormalizedValueForDomainValue(SdDataAttributeConstant.INS_STATUS, insStatusValue, dataSource);
			
			if (normalizedValueForDomainValue == null) {
				log.error("no normalized status value found for instrument dataContainer : {} for domain value", container,
						insStatusValue);
				return false;
			}

			return normalizedValueForDomainValue.equals(DomainStatus.INACTIVE);
		}
}
