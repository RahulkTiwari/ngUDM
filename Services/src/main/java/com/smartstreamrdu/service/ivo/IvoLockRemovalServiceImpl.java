/**
* Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
* All rights reserved.
* 
* File: IvoLockRemovalServiceImpl.java
* Author : Padgaonkar
* Date : April 10, 2019
* 
*/
package com.smartstreamrdu.service.ivo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.HistorizedData;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.domain.autoconstants.EnDataAttrConstant;
import com.smartstreamrdu.persistence.service.PersistenceServiceImpl;
import com.smartstreamrdu.service.audit.AuditService;
import com.smartstreamrdu.service.listener.ListenerService;
import com.smartstreamrdu.service.merging.ChildContainerComparator;
import com.smartstreamrdu.service.merging.ContainerComparatorFactory;
import com.smartstreamrdu.util.Constant.ListenerConstants;
import com.smartstreamrdu.util.IvoConstants;
import com.smartstreamrdu.util.LambdaExceptionUtil;

@Component
public class IvoLockRemovalServiceImpl implements IvoLockRemovalService {

	protected static final DataAttribute INSTRUMENT_ID_ATTR = DataAttributeFactory
			.getObjectIdIdentifierForLevel(DataLevel.INS);
	protected static final DataAttribute INSTRUMENT_SRCUNQID_ATTR = DataAttributeFactory
			.getSourceUniqueIdentifierForLevel(DataLevel.INS);
	protected static final DataAttribute SECURITY_SRCUNQID_ATTR = DataAttributeFactory
			.getSourceUniqueIdentifierForLevel(DataLevel.SEC);
	protected static final DataAttribute SECURITY_ID_ATTR = DataAttributeFactory
			.getObjectIdIdentifierForLevel(DataLevel.SEC);
	protected static final DataAttribute LE_ID_ATTR = DataAttributeFactory
			.getObjectIdIdentifierForLevel(DataLevel.LE);
	protected static final DataAttribute LE_SRCUNQID_ATTR = DataAttributeFactory
			.getSourceUniqueIdentifierForLevel(DataLevel.LE);
	protected static final DataAttribute DATASOURCE_ATTR = DataAttributeFactory.getDatasourceAttribute();
	protected static final DataAttribute DOC_TYPE_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(IvoConstants.DOC_TYPE, DataLevel.IVO_DOC);
	protected static final DataAttribute UPD_DATE_SD_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateDate, DataLevel.Document);
	protected static final DataAttribute EN_UPD_DATE_SD_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateDate, DataLevel.EN);
	
	protected static final DataAttribute UPD_DATE_IVO_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateDate, DataLevel.IVO_DOC);
	protected static final DataAttribute UPD_USER_SD_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateUser, DataLevel.Document);

	protected static final DataAttribute UPD_USER_IVO_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateUser, DataLevel.IVO_DOC);
	protected static final DataAttribute INS_DATE_SD_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertDate, DataLevel.Document);

	protected static final DataAttribute INS_DATE_IVO_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertDate, DataLevel.IVO_DOC);
	protected static final DataAttribute INS_USER_SD_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertUser, DataLevel.Document);

	protected static final DataAttribute INS_USER_IVO_ATTR = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertUser, DataLevel.IVO_DOC);

	private static final List<DataAttribute> attributeList = Arrays.asList(INSTRUMENT_ID_ATTR, INSTRUMENT_SRCUNQID_ATTR,
			SECURITY_SRCUNQID_ATTR, SECURITY_ID_ATTR, DATASOURCE_ATTR, DOC_TYPE_ATTR, UPD_DATE_SD_ATTR,
			UPD_DATE_IVO_ATTR, UPD_USER_SD_ATTR, UPD_USER_IVO_ATTR, INS_DATE_SD_ATTR, INS_DATE_IVO_ATTR,
			INS_USER_SD_ATTR, INS_USER_IVO_ATTR, LE_ID_ATTR, LE_SRCUNQID_ATTR,EnDataAttrConstant.EVENT_SOURCE_UNIQUE_ID, EnDataAttrConstant.DATA_SOURCE,
			EnDataAttrConstant.DATA_SOURCE, EnDataAttrConstant.UPD_USER, EnDataAttrConstant.INS_DATE,
			EnDataAttrConstant.INS_USER);

	@Autowired
	private PersistenceServiceImpl persistenceService;

	@Autowired
	private ContainerComparatorFactory containerComparatorFactory;

	@Autowired
	private ListenerService listenerService;
	
	@Autowired
	private AuditService auditService;

	@Override
	public void removeLockFromDataContainer(DataContainer deletedContainer, DataContainer dbContainer,
			String dataSource) throws UdmTechnicalException {
		Objects.requireNonNull(deletedContainer, "deletedContainer should be populated");
		Objects.requireNonNull(dbContainer, "dbContainer should be populated");

		dbContainer.setNew(false);
		removeAttributesValueFromContainer(deletedContainer, dbContainer);

		List<DataContainer> childDataContainers = deletedContainer.getAllChildDataContainers();

		if (!CollectionUtils.isEmpty(childDataContainers)) {
			childDataContainers.forEach(LambdaExceptionUtil
					.rethrowConsumer(childContainer -> removeAttributesValueFromContainer(childContainer,
							getDbChildContainer(deletedContainer, dbContainer, childContainer))));

		}

		dbContainer.setHasChanged(true);
		dbContainer.updateDataContainerContext(deletedContainer.getDataContainerContext());
        createAuditAndSendContainerEvents(deletedContainer, dbContainer, dataSource);
		persistenceService.persist(dbContainer);
		listenerService.dataContainerUpdated(dataSource, deletedContainer, dbContainer, null);
	}
	
	private void createAuditAndSendContainerEvents(DataContainer deletedContainer, DataContainer dbContainer, String dataSource) {
        auditService.createAudit(dbContainer);
        listenerService.dataContainerMerge(dataSource, deletedContainer, dbContainer);
        listenerService.mergeComplete(dataSource, deletedContainer, dbContainer);
    }

	/**
	 * The method will return matching child dbContainer with input Container.
	 * 
	 * @param deletedContainer
	 * @param dbContainer
	 * @param childContainer
	 * @return
	 * @throws UdmTechnicalException
	 */
	private DataContainer getDbChildContainer(DataContainer deletedContainer, DataContainer dbContainer,
			DataContainer childContainer) throws UdmTechnicalException {
		DataContainer dbChildCotainer = null;
		dbChildCotainer = getChildContainerForData(childContainer, dbContainer, deletedContainer);
		if (dbChildCotainer == null) {
			throw new UdmTechnicalException("DB child container is not found for input container", new Exception());
		}
		return dbChildCotainer;
	}

	/**
	 * The method will remove lockValues in dbContainer for all attributes from
	 * deletedContainer.
	 * 
	 * @param deletedContainer
	 * @param dbContainer
	 */
	private void removeAttributesValueFromContainer(DataContainer deletedContainer, DataContainer dbContainer) {
		DataRow attributesTobeDeleted = deletedContainer.getDataRow();
		Map<DataAttribute, DataValue<Serializable>> rowData = attributesTobeDeleted.getRowData();

		for (Entry<DataAttribute, DataValue<Serializable>> mapEntry : rowData.entrySet()) {

			DataAttribute dataAttribute = mapEntry.getKey();

			if (!isAttributePresentInDbContainer(dbContainer, mapEntry.getKey())) {
				continue;
			}

			DataType dataType = dataAttribute.getDataType();

			switch (dataType) {

			case NESTED_ARRAY:
				IvoLockRemovalHelper.handleForNestedArrayAttributes(deletedContainer, dataAttribute, dbContainer);
				break;

			case NESTED:
				IvoLockRemovalHelper.handleForNestedAttribute(dataAttribute, deletedContainer, dbContainer);
				break;
			default:
				removeLockValueForSimpleAttribute(dbContainer, dataAttribute, rowData.get(dataAttribute));
				break;

			}
		}
	}

	/**
	 * The method remove locks for Simple Attributes having DataType like
	 * String,Integer etc.
	 * 
	 * @param dbContainer
	 * @param toBeDeletedAttribute
	 * @param tobeDeletedDataValue
	 */
	private void removeLockValueForSimpleAttribute(DataContainer dbContainer, DataAttribute toBeDeletedAttribute,
			DataValue<Serializable> tobeDeletedDataValue) {

		if (isItContainInShouldNotDeleteAttributeList(toBeDeletedAttribute)) {
			return;
		}
		DataRow dbAttribute = dbContainer.getDataRow();
		Map<DataAttribute, DataValue<Serializable>> rowData = dbAttribute.getRowData();
		DataValue<Serializable> dbDataValue = rowData.get(toBeDeletedAttribute);

		Map<LockLevel, HistorizedData<Serializable>> lockData = tobeDeletedDataValue.getLockData();

		lockData.keySet().forEach(dbDataValue::removeLockValue);

	}

	/**
	 * When we create deletedContainer the unique Identifier are added into
	 * container in order to retrive db container.but while iterating for deletion
	 * those attributes are need to be excluded.
	 * 
	 * @param toBeDeletedAttribute
	 * @return
	 */
	private boolean isItContainInShouldNotDeleteAttributeList(DataAttribute toBeDeletedAttribute) {
		return attributeList.contains(toBeDeletedAttribute);
	}

	/**
	 * If given attribute contains in db container method will return true else it.
	 * will return false
	 * 
	 * @param dbContainer
	 * @param attribute
	 * @return
	 */
	private boolean isAttributePresentInDbContainer(DataContainer dbContainer, DataAttribute attribute) {

		DataRow dbAttribute = dbContainer.getDataRow();
		Map<DataAttribute, DataValue<Serializable>> rowData = dbAttribute.getRowData();
		Set<DataAttribute> keySet = rowData.keySet();
		return keySet.contains(attribute);
	}

	/**
	 * 
	 * @param childContainer
	 * @param dbDataContainer
	 * @param inContainer
	 * @return
	 * @throws Exception
	 */
	private DataContainer getChildContainerForData(DataContainer childContainer, DataContainer dbDataContainer,
			DataContainer inContainer) throws UdmTechnicalException {
		try {

			ChildContainerComparator childContainerComparator = containerComparatorFactory
					.getChildContainerComparator(inContainer);
			return childContainerComparator.compare(inContainer, childContainer,
					dbDataContainer.getAllChildDataContainers());
		} catch (Exception e) {
			throw new UdmTechnicalException("Following Error occured while retriving container from db", e);
		}
	}

}
