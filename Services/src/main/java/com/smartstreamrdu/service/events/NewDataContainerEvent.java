package com.smartstreamrdu.service.events;


import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.Audit;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataStorageEnum;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.util.Constant.ListenerConstants;

@Component
public class NewDataContainerEvent implements EventListener<DataContainer> {
		
	private static final Logger _logger = LoggerFactory.getLogger(NewDataContainerEvent.class);
	
	@Override
	public void propogateEvent(DataContainer container) {		
			
		DataValue<String> updUserValue = new DataValue<>();
		
					
		
		DataValue<LocalDateTime> valuesdt = new DataValue<>();
		Audit latestAudit = container.getLatestAudit();
		valuesdt.setValue(LockLevel.RDU, latestAudit!=null?container.getLatestAudit().getUpdateDate():LocalDateTime.now());
		
		
		setInsUserInsDate(container, updUserValue, valuesdt);
	}

	private void setInsUserInsDate(DataContainer container, DataValue<String> updUserValue,
			DataValue<LocalDateTime> valuesdt) {
		String updUser=container.getDataContainerContext().getUpdatedBy();
		
		updUser=!StringUtils.isEmpty(updUser)? updUser: ListenerConstants.updatedBy;

	/* add insertUser insertDate in data Container
	 * for INS/LE get insUser/insDate at Document Level
	 * for EN get insUser/insDate at EN level
	 */

		DataStorageEnum dataStorageEnum = DataStorageEnum.getStorageByLevel(container.getLevel());
		//IVO_INS or INS/LE/EN
		if (dataStorageEnum != null||container.getLevel()==DataLevel.IVO_INS) {
			updUserValue.setValue(LockLevel.RDU, updUser);
			container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertUser,
					container.getLevel().getRootLevel()), updUserValue);
			container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertDate,
					container.getLevel().getRootLevel()), valuesdt);
		}
	}

	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return ListenerEvent.NewDataContainer == event;
	}
		
	@Override
	public DataContainer createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		
		if (!(inputCreationContext instanceof NewDataContainerChangeEventListenerInputCreationContext)) {
			_logger.error("{} requires a input creation context of type UpdateChangeEventListenerInputCreationContext. Supplied input creation context was of type {}", this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException("Argument of incorrect type supplied input creation is not supported for "+this.getClass().getName());
		}
		
		NewDataContainerChangeEventListenerInputCreationContext inputContext = (NewDataContainerChangeEventListenerInputCreationContext) inputCreationContext;
		
		return inputContext.getDataContainer();
	}

}
