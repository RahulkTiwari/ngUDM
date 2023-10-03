/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdIvoToSdContainerChildComparator.java
 * Author : SaJadhav
 * Date : 14-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.util.Constant.CrossRefConstants;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.IvoConstants;

/**
 * Compares child sdIvoContainer with list of child sdContainer and returns matching child sdContainer
 * @author SaJadhav
 *
 */
@Component
public class SdIvoToSdContainerChildComparator{
	
	private final DataAttribute rduSecIdAttr = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.RDU_ID_SEC_ATT_NAME, DataLevel.XRF_SEC);
	
	private final DataAttribute xrfSecurityStatucAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.XR_SEC_STATUS, DataLevel.XRF_SEC);
	
	private final DataAttribute secRelAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel(IvoConstants.SECURITY_RELATIONS, DataLevel.IVO_SEC);
	

	private final DataAttribute xrSecLinksAttr = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.SEC_LINKS, DataLevel.XRF_SEC);
	private final DataAttribute secXrfLinkStatusAttr = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.XR_SEC_LINK_STATUS, DataLevel.XRF_SEC);
	private final DataAttribute secXrfRefIdAttr = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.SEC_REF_ID, DataLevel.XRF_SEC);
	
	private final DataAttribute sdObjectIdAttribute = DataAttributeFactory.getObjectIdIdentifierForLevel(DataLevel.SEC);

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.ivo.SdIvoToSdContainerComparator#compareAndReturn(com.smartstreamrdu.domain.DataContainer, java.util.List, com.smartstreamrdu.domain.DataContainer)
	 */
	public Optional<DataContainer> findMatchingContainerByIvo(DataContainer sdIvoChildContainer, List<DataContainer> sdChildContainers,
			DataContainer xrDataContainer,String sdDocumentId) {
		Objects.requireNonNull(sdIvoChildContainer, "sdIvoChildContainer should be populated");
		Objects.requireNonNull(sdChildContainers, "sdChildContainers should be populated");
		Objects.requireNonNull(xrDataContainer, "xrDataContainer should be populated");
		Objects.requireNonNull(sdDocumentId, "sdDocumentId should be populated");
		
		ReferenceId rduIdAndDocId = getDataContainerFromIvo(sdIvoChildContainer);
		
		String rduId = rduIdAndDocId!=null?rduIdAndDocId.getObjectId():null;
		String comparisonSecurityId = getSecurityIdFromSdSchema(xrDataContainer, sdDocumentId, rduId);
			
		if (StringUtils.isNotBlank(comparisonSecurityId)) {
			return getSdChildContainer(sdChildContainers, comparisonSecurityId);
		}
		return Optional.empty();
	}

	/**
	 * @param sdChildContainers
	 * @param comparisonSecurityId
	 */
	private Optional<DataContainer> getSdChildContainer(List<DataContainer> sdChildContainers, String comparisonSecurityId) {
		for (DataContainer sdChildContainer : sdChildContainers) {
			String securityId = sdChildContainer.getHighestPriorityValue(sdObjectIdAttribute);
			if (securityId.equals(comparisonSecurityId)) {
				return Optional.of(sdChildContainer);
			}
		}
		return Optional.empty();
	}

	private ReferenceId getDataContainerFromIvo(DataContainer sdIvoChildContainer) {
		return getIvoSecRelationRefId(sdIvoChildContainer);
	}
	
	/**
	 * Gets rduSecurityId from the list of xrf security containers
	 * 
	 * @param sdIvoChildContainer
	 * @param xrContainer
	 * @param sdDocumentId
	 * @param securityId 
	 * @return
	 */
	private String getSecurityIdFromSdSchema(DataContainer xrContainer, String sdDocumentId, String rduSecurityId) {

		List<DataContainer> matchingXrSecs = getMatchingXrfSecurities(xrContainer, rduSecurityId);
		if (CollectionUtils.isNotEmpty(matchingXrSecs)) {
			DataContainer matchingXrSecurity = matchingXrSecs.get(0);
			return getObjectIdFromXrSecurity(matchingXrSecurity, sdDocumentId);
		}
		return "";
	}

	private List<DataContainer> getMatchingXrfSecurities(DataContainer xrContainer, String rduSecurityId) {
		//get matching xrf security based on rduSecurityId
		return xrContainer.getAllChildDataContainers().stream()
				.filter(xrChildContainer ->  matchActiveRduSecId(rduSecurityId, xrChildContainer)).collect(Collectors.toList());
	}

	private boolean matchActiveRduSecId(String rduSecurityId, DataContainer xrChildContainer) {
		String rduSecId = (String) xrChildContainer.getAttributeValueAtLevel(LockLevel.RDU, rduSecIdAttr);
		DomainType xrfSecurityStatus = (DomainType) xrChildContainer.getAttributeValueAtLevel(LockLevel.RDU,
				xrfSecurityStatucAttribute);

		return rduSecurityId.equals(rduSecId)
				&& DomainStatus.ACTIVE.equals(xrfSecurityStatus.getNormalizedValue());
	}
	
	/**
	 * @param matchingXrSecurity
	 * @param sdDocumentId
	 * @return
	 */
	private String getObjectIdFromXrSecurity(DataContainer matchingXrSecurity, String sdDocumentId) {
		
		String objectId=null;
		
		
		DataRowIterator iterator=new DataRowIterator(matchingXrSecurity, xrSecLinksAttr);
		while(iterator.hasNext()){
			DataRow xrSecurityLink = iterator.next();
			DomainType secLinkStatus=xrSecurityLink.getAttributeValueAtLevel(LockLevel.RDU, secXrfLinkStatusAttr);
			ReferenceId secXrfRefId=xrSecurityLink.getAttributeValueAtLevel(LockLevel.RDU, secXrfRefIdAttr);
			
			if( DomainStatus.ACTIVE.equals(secLinkStatus.getNormalizedValue()) &&
					sdDocumentId.equals(secXrfRefId.getDocumentId())){
				objectId=secXrfRefId.getObjectId();
				break;
			}
			
		}
		return objectId;
	}

	/**
	 * @param dbContainer
	 * @param secRelAttribute
	 */
	private ReferenceId getIvoSecRelationRefId(DataContainer dbContainer) {
		DataRowIterator iterator=new DataRowIterator(dbContainer, secRelAttribute);
		ReferenceId output=null;
		while(iterator.hasNext()){
			DataRow dataRow = iterator.next();
			String relaTionTypeVal=dataRow.getAttributeValueAtLevel(LockLevel.RDU, 
					DataAttributeFactory.getRelationTypeAttribute(secRelAttribute));
			if(RelationType.IVO.name().equals(relaTionTypeVal)){
				output=dataRow.getAttributeValueAtLevel(LockLevel.RDU, 
						DataAttributeFactory.getRelationRefIdAttribute(secRelAttribute));
				break;
			}
		}
		return output;
	}

}
