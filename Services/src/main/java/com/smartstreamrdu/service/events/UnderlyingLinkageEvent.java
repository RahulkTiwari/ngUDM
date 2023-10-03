package com.smartstreamrdu.service.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.FeedConfiguration;
import com.smartstreamrdu.domain.LookupAttributes;
import com.smartstreamrdu.domain.message.LinkageMessage;
import com.smartstreamrdu.domain.message.UdmMessageKey;
import com.smartstreamrdu.events.ChangeEventInputPojo;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.service.messaging.DefaultMessage;
import com.smartstreamrdu.service.messaging.Message;
import com.smartstreamrdu.service.messaging.producer.Producer;
import com.smartstreamrdu.service.messaging.producer.ProducerEnum;
import com.smartstreamrdu.service.messaging.producer.ProducerFactory;
import com.smartstreamrdu.util.Constant.ListenerConstants;
import com.smartstreamrdu.util.Constant.Process;

@Component
public class UnderlyingLinkageEvent implements EventListener<ChangeEventInputPojo> {

	static final Logger _logger = LoggerFactory.getLogger(UnderlyingLinkageEvent.class);

	private static final DataAttribute dataSourceAttr = DataAttributeFactory
			.getAttributeByNameAndLevel(ListenerConstants.dataSource, DataLevel.Document);
	private static final DataAttribute securityId = DataAttributeFactory
			.getAttributeByNameAndLevel(ListenerConstants.securityId, DataLevel.SEC);

	@Autowired
	private ProducerFactory producerFactory;
	
	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;
	
	@SuppressWarnings("unchecked")
	@Override
	public void propogateEvent(ChangeEventInputPojo changeEventPojo) {

		DataContainer container=changeEventPojo.getPostChangeContainer();
		FeedConfiguration feedConfiguration = changeEventPojo.getFeedConfiguration();
		if(container==null || container.getLevel() !=DataLevel.INS){
			return;
		}

		List<DataContainer> childContainers = container.getAllChildDataContainers();
		DataValue<DomainType> dataSourceVal = (DataValue<DomainType>) (container.getAttributeValue(dataSourceAttr));
		String dataSourceValue=dataSourceVal.getValue().getVal();
		for (DataContainer childContainer : childContainers) {
			checkSecurityContainer(container, childContainer, container.get_id(),dataSourceValue,feedConfiguration);
		}
	}

	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return ListenerEvent.DataUpdate == event;
	}

	@Override
	public ChangeEventInputPojo createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		
		if (!(inputCreationContext instanceof UpdateChangeEventListenerInputCreationContext)) {
			_logger.error("{} requires a input creation context of type UpdateChangeEventListenerInputCreationContext. Supplied input creation context was of type {}", this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException("Argument of incorrect type supplied input creation is not supported for "+this.getClass().getName());
		}
		
		UpdateChangeEventListenerInputCreationContext inputContext = (UpdateChangeEventListenerInputCreationContext) inputCreationContext;
		
		ChangeEventInputPojo inp = new ChangeEventInputPojo();
		inp.setPreChangeContainer(inputContext.getFeedDataContaier());
		inp.setPostChangeContainer(inputContext.getDbDataContainer());
		inp.setFeedConfiguration(inputContext.getFeedConfiguration());
		return inp;
	}


	private void checkSecurityContainer(DataContainer parentContainer, DataContainer childContainer, String id, String dataSource,FeedConfiguration feedConfiguration) {

		LookupAttributes lookupAttributes = getLookupAttributesFromFeedConfig(feedConfiguration);
		if(lookupAttributes ==null){
			return;
		}
		if (childContainer.isNew() || isLoookupAttributeChanged(lookupAttributes,childContainer,dataSource)) {
			sendMessageToQueue(parentContainer, childContainer, id, dataSource,lookupAttributes);
		}
	}

	@SuppressWarnings("rawtypes")
	private boolean isLoookupAttributeChanged(LookupAttributes lookupAttributes, DataContainer childContainer, String dataSource) {
		DataStorageEnum dataStorage = cacheDataRetrieval.getDataStorageFromDataSource(dataSource);
		for ( List<String> lstLookUpAttrs : lookupAttributes.getAttributes()) {
				for (String lookUpAttr : lstLookUpAttrs) {	
					DataAttribute dataAttr = dataStorage.getAttributeByName(lookUpAttr);
					DataValue dv = (DataValue) childContainer.getAttributeValue(dataAttr);
					if(dv!=null && !dv.getPreviousValueMap().isEmpty()){
						return true;
					}
				}
		}
		return false;
	}

	private LookupAttributes getLookupAttributesFromFeedConfig(FeedConfiguration feedConfiguration) {
		LookupAttributes lookupAttributes=null;
	
		if(feedConfiguration == null){
			return null;
		}
		
		for (LookupAttributes las : feedConfiguration.getLookupAttributes()) {
			if(DataLevel.INS.name().equals(las.getLevel())){
				lookupAttributes=las;
			}
		}
		return lookupAttributes;
	}

	private void sendMessageToQueue(DataContainer parentContainer, DataContainer childContainer, String id, String dataSource,
			LookupAttributes lookupAttributes) {
		LinkageMessage message = createLinkageMessageObject(parentContainer, childContainer, id, dataSource, lookupAttributes);
		
		if(CollectionUtils.isEmpty(message.getLookupAttributes()) ) {
			return;
		}

		UdmMessageKey udmMessageKey = UdmMessageKey.builder().action(Process.LinkageJob.name())
				.variableAttributeValue(message.getDocumentId()).build();

		Message input = new DefaultMessage.Builder().data(message).key(udmMessageKey)
				.target(com.smartstreamrdu.util.Constant.Component.DATA_ENRICHMENT).process(Process.LinkageJob).build();

		Producer<?> producer = producerFactory.getProducer(ProducerEnum.Kafka);

		try {
			producer.sendMessage(input);
			_logger.debug("Message sent to Data Enrichment with details: {}", input);
		} catch (Exception e) {
			_logger.error("Following error occured while sending message to Data Enrichment", e);
		}

	}

	@SuppressWarnings({ "rawtypes" })
	private LinkageMessage createLinkageMessageObject(DataContainer parentContainer, DataContainer childContainer, String id, String dataSource, LookupAttributes lookupAttributes) {

		LinkageMessage message = new LinkageMessage();

		message.setDataSource(dataSource);
		message.setDocumentId(id);
		message.setObjectId(((DataValue) childContainer.getAttributeValue(securityId)).getValue().toString());
		message.setLookupAttributes(createLookupAttributesList(parentContainer, childContainer,lookupAttributes,dataSource));

		return message;
	}

	private List<Map<String, Serializable>> createLookupAttributesList(DataContainer parentContainer, DataContainer childContainer, LookupAttributes lookupAttributes, String dataSource) {
		List<Map<String, Serializable>> lookupAttributesList = new ArrayList<>();
		for (List<String> lstLookUpAttrs : lookupAttributes.getAttributes()) {
			Map<String, Serializable> attributesMap = new HashMap<>();
			for (String lookUpAttr : lstLookUpAttrs) {		
				//If value is not null then only adding into Map.
				//UseCase:In case of technical security as securitySourceUniqueId is not present no need to send message to DEN.
				DataStorageEnum dataStorage = cacheDataRetrieval.getDataStorageFromDataSource(dataSource);
				DataAttribute attribute = dataStorage.getAttributeByName(lookUpAttr);
				
				Serializable dV = null;
				
				if (attribute.getAttributeLevel().getParentLevel() == null ) {
					dV = getValueFromContainer(parentContainer, attribute);
				} else {
					dV = getValueFromContainer(childContainer, attribute);
				}
				
				if (dV != null) {
					attributesMap.put(lookUpAttr, dV);
				}
			}
			if (!attributesMap.isEmpty()) {
				lookupAttributesList.add(attributesMap);
			}
		}
		return lookupAttributesList;
	}

	/**
	 *  Returns the highest priority value from the given attribute
	 *  from the given data container.
	 * 
	 * @param container
	 * @param attribute
	 * @return
	 */
	private Serializable getValueFromContainer(DataContainer container, DataAttribute attribute) {
		return container.getHighestPriorityValue(attribute);
	}
}
