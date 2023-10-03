/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: IvoQueryServiceImpl.java
* Author : Padgaonkar
* Date : April 10, 2019
* 
*/

package com.smartstreamrdu.service.ivo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.IvoDocType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoInstrumentAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoInstrumentRelationAttrConstant;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrievalService;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.Database;
import com.smartstreamrdu.util.Constant.CrossRefConstants;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.Constant.SdAttributeNames;
import com.smartstreamrdu.util.DataContainerUtil;
import com.smartstreamrdu.util.IvoConstants;

import lombok.NonNull;

@Component
public class IvoQueryServiceImpl implements IvoQueryService{
	
	private final DataAttribute ivoInstrumentRelationsAttr = DataAttributeFactory.getAttributeByNameAndLevel(SdAttributeNames.INSTRUMENT_RELATIONS, DataLevel.IVO_INS);
	private final DataAttribute ivoRelationsReferenceIdAttr = DataAttributeFactory.getRelationRefIdAttribute(ivoInstrumentRelationsAttr);
	private final DataAttribute ivoRelationsRelationTypeAttr=DataAttributeFactory.getRelationTypeAttribute(ivoInstrumentRelationsAttr);
	private final DataAttribute docTypeAttr = DataAttributeFactory.getAttributeByNameAndLevel(IvoConstants.DOC_TYPE, DataLevel.IVO_DOC);
	private final DataAttribute xrInsLinksAttr = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.INS_LINKS,
			DataLevel.XRF_INS);
	
	@Autowired
	private DataRetrievalService retrievalService;
	

	/**
	 * The method will return matchingDataContainers from SdData.
	 */
	@Override
	public List<DataContainer> getMatchingDataContainerByDocumentId(DataContainer dataContainer) throws UdmTechnicalException {
	
		Criteria criteria=Criteria.where( DataAttributeFactory.getIdDataAttributeForDataLevel(dataContainer.getLevel(),false,null));
		DataValue<String> idValue=new DataValue<>();
		idValue.setValue(LockLevel.FEED, dataContainer.get_id());
		criteria=criteria.is(idValue);
		return retrieveFromDatabase(criteria);
		
	}

	private List<DataContainer> retrieveFromDatabase(Criteria criteria) throws UdmTechnicalException {
		DataRetrivalInput input=new DataRetrivalInput();
		input.setCriteria(criteria);
		List<DataContainer> retrievedContainers = retrievalService.retrieve(Database.Mongodb,input);
		if(CollectionUtils.isNotEmpty(retrievedContainers)){
			DataContainerUtil.populateNewFlagAndHasChanged(retrievedContainers);
		}
		return retrievedContainers;
	}

	/**
	 * 	  The method will return matchingLeDataContainer from SdData
	 */
	@Override
	public List<DataContainer> getMatchingLegalEntityDataContainerFromSdData(DataContainer sdLeContainer) throws UdmTechnicalException {
		
		DataAttribute legalEntitySrcUnqIdAttr = DataAttributeFactory.getSourceUniqueIdentifierForLevel(DataLevel.LE);
		String legalEntitySourceUniqueId = (String) sdLeContainer.getAttributeValueAtLevel(LockLevel.FEED, legalEntitySrcUnqIdAttr);
		DataValue<String> legalEntityIdVal = new DataValue<>();
		legalEntityIdVal.setValue(LockLevel.FEED, legalEntitySourceUniqueId);
		Criteria legalEnitityIdCrit = Criteria.where(legalEntitySrcUnqIdAttr).is(legalEntityIdVal);
		return retrieveFromDatabase(legalEnitityIdCrit);
    }

	/**
	 * 	  The method will return matchingLeDataContainer from SdIvo
	 */
	@Override
     public List<DataContainer> getMatchingDataContainerFromSdIvo(DataContainer sdIvoContainer) throws UdmTechnicalException {
		ReferenceId referenceId = getIvoReferenceId(sdIvoContainer);
		DataValue<ReferenceId> referenceIdVal=new DataValue<>();
		referenceIdVal.setValue(LockLevel.RDU, referenceId);
		Criteria referenceIdCriteria=Criteria.where(ivoRelationsReferenceIdAttr).is(referenceIdVal);
		DataValue<String> docTypeVal=new DataValue<>();
		docTypeVal.setValue(LockLevel.RDU, IvoDocType.RDU.name());
		
		Criteria docTypeCriteria = Criteria.where(docTypeAttr).is(docTypeVal);
		
		DataValue<String> relationTypeVal=new DataValue<>();
		relationTypeVal.setValue(LockLevel.RDU, RelationType.IVO.name());
		Criteria relationTypeCriteria=Criteria.where(ivoRelationsRelationTypeAttr).is(relationTypeVal);
		
		referenceIdCriteria=referenceIdCriteria.andOperator(docTypeCriteria,relationTypeCriteria);
		
		return retrieveFromDatabase(referenceIdCriteria);
		
	}
	
	/**
	 * @param sdIvoContainer
	 * @param ivoInstrumentRelationsAttr
	 * @param ivoRelationsReferenceIdAttr
	 * @return
	 */
	private ReferenceId getIvoReferenceId(DataContainer sdIvoContainer) {
		DataRowIterator iterator = new DataRowIterator(sdIvoContainer, ivoInstrumentRelationsAttr);
		while(iterator.hasNext()) {
			DataRow dataRow = iterator.next();
			String relationType = dataRow.getHighestPriorityAttributeValue(ivoRelationsRelationTypeAttr);
			if(RelationType.IVO.name().equals(relationType)) {
				return dataRow.getAttributeValueAtLevel(LockLevel.RDU, ivoRelationsReferenceIdAttr);
			}
		}
		return null;
	}
	
	/**
	 * Retruns XRF dataContainer from instrumentId and sdDocumentId
	 * @param instrumentId
	 * @param sdDocumentId
	 * @return
	 */
	@Override
	public List<DataContainer> getXrfDataContainer(String instrumentId, String sdDocumentId)
			throws UdmTechnicalException {
		Criteria xrCriteria = getXrfCriteria(instrumentId,sdDocumentId);
		DataRetrivalInput input=new DataRetrivalInput();
		input.setCriteria(xrCriteria);
		return retrievalService.retrieve(Database.Mongodb,input);
	}
	

	/**
	 * Returns XRF criteria on xrInstrumentLinks referenceId and
	 * instrumentXrfLinkStatus
	 * 
	 * @param ivoContainer
	 * @param sdIvoContainer
	 * @return
	 */
	private Criteria getXrfCriteria(String instrumentId,String sdDocumentId) {
		DataAttribute xrInsLinksRefd = DataAttributeFactory
				.getAttributeByNameAndLevelAndParent(CrossRefConstants.INS_REF_ID, DataLevel.XRF_INS, xrInsLinksAttr);
		DataValue<ReferenceId> xrInsLinkRefDataVal = new DataValue<>();
		ReferenceId xrInsLinkRef = new ReferenceId(instrumentId, sdDocumentId);
		xrInsLinkRefDataVal.setValue(LockLevel.RDU, xrInsLinkRef);
		Criteria insLinkRefIdCriteria = Criteria.where(xrInsLinksRefd).is(xrInsLinkRefDataVal);

		DataValue<DomainType> statusActiveVal = new DataValue<>();
		statusActiveVal.setValue(LockLevel.RDU, new DomainType(null, null, DomainStatus.ACTIVE));
		Criteria insLinkStatus = Criteria.where(DataAttributeFactory
				.getAttributeByNameAndLevel(CrossRefConstants.XR_INS_LINK_STATUS, DataLevel.XRF_INS))
				.is(statusActiveVal);

		return new Criteria().andOperator(insLinkRefIdCriteria, insLinkStatus);
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public DataContainer getXrfDataContainerFromIvoDataContainer(@NonNull DataContainer ivoDataContainer) throws UdmTechnicalException {
		
		if(!DataLevel.IVO_INS.equals(ivoDataContainer.getLevel())) {
			throw new IllegalArgumentException("input ivoDataContainer should have IVO_INS level");
		}

		Object xrfDocumentId=getXrfDocumentId(ivoDataContainer);
		if(xrfDocumentId==null) {
			return null;
		}
		DataValue<String> documentIdVal=new DataValue<>();
		documentIdVal.setValue(LockLevel.NONE, xrfDocumentId.toString());
		Criteria criteria = Criteria.where(DataAttributeFactory.getIdDataAttributeForDataLevel(DataLevel.XRF_INS, false, LockLevel.NONE)).is(documentIdVal);
		DataRetrivalInput input=new DataRetrivalInput();
		input.setCriteria(criteria);
		List<DataContainer> dataContainers= retrievalService.retrieve(Database.Mongodb, input);
		if(CollectionUtils.isNotEmpty(dataContainers)) {
			return dataContainers.get(0);
		}
		return null;
	}
	
	/**
	 * @param ivoDataContainer
	 * @return
	 */
	private Object getXrfDocumentId(DataContainer ivoDataContainer) {
		DataRow instrumentRelationsRow = (DataRow) ivoDataContainer
				.getAttributeValue(IvoInstrumentAttrConstant.INSTRUMENT_RELATIONS);
		ArrayList<DataRow> listInstrumentRelations = instrumentRelationsRow.getValue().getValue();
		Optional<DataRow> filter = listInstrumentRelations.stream()
				.filter(this :: isIvoRelation).findFirst();
		if(filter.isPresent()) {
			ReferenceId referenceId = filter.get().getHighestPriorityAttributeValue(DataAttributeFactory
					.getAttributeByNameAndLevelAndParent(IvoInstrumentRelationAttrConstant.COL_REFERENCE_ID,
							DataLevel.IVO_INS, IvoInstrumentAttrConstant.INSTRUMENT_RELATIONS));
			return referenceId.getDocumentId();
		}
		return null;
	}

	/**
	 * @param instrumentRelation
	 * @return
	 */
	private boolean isIvoRelation(DataRow instrumentRelation) {
		String relationType = instrumentRelation.getHighestPriorityAttributeValue(DataAttributeFactory
				.getAttributeByNameAndLevelAndParent(IvoInstrumentRelationAttrConstant.COL_RELATION_TYPE,
						DataLevel.IVO_INS, IvoInstrumentAttrConstant.INSTRUMENT_RELATIONS));
		return RelationType.IVO.name().equals(relationType);
	}
}
