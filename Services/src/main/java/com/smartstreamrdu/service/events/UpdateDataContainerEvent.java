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
import com.smartstreamrdu.events.InactiveBean;
import com.smartstreamrdu.service.listener.ListenerEvent;
import com.smartstreamrdu.util.Constant.ListenerConstants;

@Component
public class UpdateDataContainerEvent implements EventListener<InactiveBean> {
	
	private static final Logger _logger = LoggerFactory.getLogger(UpdateDataContainerEvent.class);
	
	@Override
	public void propogateEvent(InactiveBean inactiveBean) {
		
		DataContainer dbcontainer = inactiveBean.getDbContainer();
	 
		// Getting the audit from instrument if there is any change in instrument
		// when Instrument gets inactivated adding audit at instrument level, returns
		// correct latestAudit value
		Audit latestAudit = dbcontainer.hasContainerChanged() ? dbcontainer.getLatestAudit() : null;
		
		if (latestAudit == null && dbcontainer.getAllChildDataContainers() != null) {
			DataContainer container = childContainerUpdated(dbcontainer);
			if(container != null){
				latestAudit = container.getLatestAudit() ;
			}
		}
		
		if (!dbcontainer.isNew() && latestAudit != null) {
			DataValue<LocalDateTime> valuesdt = new DataValue<>();
			valuesdt.setValue(LockLevel.RDU, latestAudit.getUpdateDate());
			setUpdUserUpdDate(dbcontainer, valuesdt,latestAudit);
		}
	}

	
	/**
	 * Check if any of the child containers are updated
	 * @param dbcontainer
	 * @param isChildContainerUpdated
	 * @return
	 */
	private DataContainer childContainerUpdated(DataContainer dbcontainer) {
		DataContainer container = null;
		for (DataContainer childContainer : dbcontainer.getAllChildDataContainers()) {
			if (childContainer.hasContainerChanged()) {
				container = childContainer;
				break;
			}
		}
		return container;
	}

	private void setUpdUserUpdDate(DataContainer dbcontainer, DataValue<LocalDateTime> valuesdt, Audit latestAudit) {
		
		String updUser=latestAudit.getUser();
		updUser=StringUtils.isNotEmpty(updUser)?updUser:ListenerConstants.updatedBy;
		
		DataValue<String> updUserValue=new DataValue<>();
		updUserValue.setValue(LockLevel.RDU, updUser);
		
		/*
		 * add insertUser updateDate in data Container
		 * for INS/LE get updUser/updDate at Document Level
		 * for EN get updUser/updDate at EN level
		 */
		DataStorageEnum dataStorageEnum = DataStorageEnum.getStorageByLevel(dbcontainer.getLevel());
		//IVO_INS or INS/LE/EN
		if (dataStorageEnum != null || dbcontainer.getLevel() == DataLevel.IVO_INS) {
			dbcontainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateDate,
					dbcontainer.getLevel().getRootLevel()), valuesdt);

			dbcontainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateUser,
					dbcontainer.getLevel().getRootLevel()), updUserValue);
		}
	}

	@Override
	public boolean isEventApplicable(ListenerEvent event) {
		return ListenerEvent.MergeComplete == event;
	}

	@Override
	public InactiveBean createInput(ChangeEventListenerInputCreationContext inputCreationContext) {
		
		if (!(inputCreationContext instanceof MergeCompleteChangeEventListenerInputCreationContext)) {
			_logger.error("{} requires a input creation context of type MergeCompleteChangeEventListenerInputCreationContext. Supplied input creation context was of type {}", this.getClass().getName(), inputCreationContext.getClass().getName());
			throw new IllegalArgumentException("Argument of incorrect type supplied input creation is not supported for "+this.getClass().getName());
		}
		
		MergeCompleteChangeEventListenerInputCreationContext inputContext = (MergeCompleteChangeEventListenerInputCreationContext) inputCreationContext;
			
		InactiveBean bean = new InactiveBean();
		bean.setDbContainer(inputContext.getDataContainer());
		bean.setDatasource(inputContext.getDataSource());
		return bean;
		
	}

}
