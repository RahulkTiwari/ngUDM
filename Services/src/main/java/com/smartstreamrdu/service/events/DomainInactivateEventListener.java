/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: DomainInactivateEventListener.java
 * Author : SaJadhav
 * Date : 19-Aug-2019
 * 
 */
package com.smartstreamrdu.service.events;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.service.domain.DomainSearchService;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.merging.DataContainerMergeException;
import com.smartstreamrdu.service.persist.DomainMergeAndPersistServiceImpl;
import com.smartstreamrdu.util.Constant.DomainStatus;

/**
 * If a domain it inactivated then it propogates the inactivation to domain map
 * @author SaJadhav
 *
 */
@Component
public class DomainInactivateEventListener implements EventListener<DataContainer> {
	
	private static final String STATUS_INACTIVE = "I";
	private static final DataAttribute RDU_DOMAIN_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevel("rduDomain", DataLevel.DV_DOMAIN_MAP);
	private static final DataAttribute NORMALIZED_VALUE_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevel("normalizedValue", DataLevel.DV_DOMAIN_MAP);
	private static final DataAttribute VENDOR_MAPPINGS_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevel("vendorMappings", DataLevel.DV_DOMAIN_MAP);
	private static final DataAttribute VENDOR_DOMAIN_NAME_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainName", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS_ATTRIBUTE);
	private static final DataAttribute VENDOR_DOMAIN_SOURCE_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainSource", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS_ATTRIBUTE);
	private static final DataAttribute VENDOR_DOMAIN_VALUE_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevelAndParent("domainValue", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS_ATTRIBUTE);
	private static final DataAttribute VENDOR_DOMAIN_STATUS_ATTRIBUTE = DataAttributeFactory.getAttributeByNameAndLevelAndParent("status", DataLevel.DV_DOMAIN_MAP,VENDOR_MAPPINGS_ATTRIBUTE);
	
	private static final Logger _logger = LoggerFactory.getLogger(DomainInactivateEventListener.class);
	
	@Autowired
	private DomainMergeAndPersistServiceImpl domainMergeAndPersistService;
	
	@Autowired
	private DomainSearchService domainSearchService;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#propogateEvent(java.io.Serializable)
	 */
	@Override
	public void propogateEvent(DataContainer mergedDomainContainer) {
		//propogation of inactive status happens from RDU domain to dvDomainMap
		if(DataLevel.DV_DOMAIN_MAP.equals(mergedDomainContainer.getLevel())){
			return;
		}
		
		DataAttribute statusAttribute = DataAttributeFactory.getAttributeByNameAndLevel("status",
				mergedDomainContainer.getLevel());
		String statusValue = mergedDomainContainer.getHighestPriorityValue(statusAttribute);
		if (!mergedDomainContainer.isNew() && mergedDomainContainer.hasContainerChanged()
				&& STATUS_INACTIVE.equals(statusValue)) {
			findAndInactivateDomainMapping(mergedDomainContainer);
		}
	}

	/**
	 * @param mergedDomainContainer
	 */
	private void findAndInactivateDomainMapping(DataContainer mergedDomainContainer) {
		DataContainer domainMappingContainer=domainSearchService.findVendorMappingsFromRduDomain(mergedDomainContainer);
		if(domainMappingContainer==null){
			return;
		}
		domainMappingContainer.updateDataContainerContext(mergedDomainContainer.getDataContainerContext());
		inactivateDomainMapping(domainMappingContainer);
	}

	/**
	 * @param domainMappingContainer
	 */
	private void inactivateDomainMapping(DataContainer domainMappingContainer) {
		DataContainer newDomainMappingsContainer=createInactiveDomainMapContainer(domainMappingContainer);
		try {
			domainMergeAndPersistService.merge(newDomainMappingsContainer, domainMappingContainer);
		} catch (DataContainerMergeException e) {
			_logger.error("Error while inactivating domain map datacontainer", e);
		}
	}

	/**
	 * @param domainMappingContainer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private DataContainer createInactiveDomainMapContainer(DataContainer domainMappingContainer) {
		DataContainer newDomainMappingsContainer = new DataContainer(domainMappingContainer.getLevel(),
				domainMappingContainer.getDataContainerContext());
		newDomainMappingsContainer.addAttributeValue(RDU_DOMAIN_ATTRIBUTE,
				(DataValue<String>) domainMappingContainer.getAttributeValue(RDU_DOMAIN_ATTRIBUTE));
		newDomainMappingsContainer.addAttributeValue(NORMALIZED_VALUE_ATTRIBUTE,
				(DataValue<String>) domainMappingContainer.getAttributeValue(NORMALIZED_VALUE_ATTRIBUTE));

		DataValue<ArrayList<DataRow>> dataVal = new DataValue<>();
		ArrayList<DataRow> dataRowList = new ArrayList<>();
		dataVal.setValue(LockLevel.RDU, dataRowList);
		DataRow newVendorMappingRow = new DataRow(VENDOR_MAPPINGS_ATTRIBUTE, dataVal);

		DataRowIterator dataRowIterator = new DataRowIterator(domainMappingContainer, VENDOR_MAPPINGS_ATTRIBUTE);
		while (dataRowIterator.hasNext()) {
			DataRow dataRow = dataRowIterator.next();
			DataValue<String> statusValue = dataRow.getAttributeValue(VENDOR_DOMAIN_STATUS_ATTRIBUTE);
			if (DomainStatus.ACTIVE.equals(statusValue.getValue())) {
				DataRow newDataRow = new DataRow(VENDOR_MAPPINGS_ATTRIBUTE);
				newDataRow.addAttribute(VENDOR_DOMAIN_NAME_ATTRIBUTE,
						dataRow.getAttributeValue(VENDOR_DOMAIN_NAME_ATTRIBUTE));
				newDataRow.addAttribute(VENDOR_DOMAIN_SOURCE_ATTRIBUTE,
						dataRow.getAttributeValue(VENDOR_DOMAIN_SOURCE_ATTRIBUTE));
				newDataRow.addAttribute(VENDOR_DOMAIN_VALUE_ATTRIBUTE,
						dataRow.getAttributeValue(VENDOR_DOMAIN_VALUE_ATTRIBUTE));
				DataValue<String> statusInactiveVal = new DataValue<>();
				statusInactiveVal.setValue(LockLevel.RDU, STATUS_INACTIVE);
				newDataRow.addAttribute(VENDOR_DOMAIN_STATUS_ATTRIBUTE, statusInactiveVal);
				dataRowList.add(newDataRow);
			}
		}
		newDomainMappingsContainer.addAttributeValue(VENDOR_MAPPINGS_ATTRIBUTE, newVendorMappingRow);
		return newDomainMappingsContainer;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#isEventApplicable(com.smartstreamrdu.service.listener.ListenerEvent)
	 */
	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return event.equals(ListenerEvent.DomainContainerUpdate);
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.events.EventListener#createInput(com.smartstreamrdu.service.events.ChangeEventListenerInputCreationContext)
	 */
	@Override
	public DataContainer createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		if (!(inputCreationContext instanceof DomainContainerChangeEventListenerInputCreationContext)) {
			_logger.error("{} requires a input creation context of type DomainContainerChangeEventListenerInputCreationContext. Supplied input creation context was of type {}", this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException("Argument of incorrect type supplied input creation is not supported for "+this.getClass().getName());
		}
		DomainContainerChangeEventListenerInputCreationContext context=(DomainContainerChangeEventListenerInputCreationContext) inputCreationContext;
		return context.getDomainContainer();
	}

}
