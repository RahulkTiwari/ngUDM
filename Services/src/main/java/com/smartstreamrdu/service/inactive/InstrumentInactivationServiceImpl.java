/**
 * 
 */
package com.smartstreamrdu.service.inactive;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.service.domain.DataContainerCloneService;
import com.smartstreamrdu.service.persist.MergeAndPersistService;
import com.smartstreamrdu.util.Constant.DomainStatus;
import com.smartstreamrdu.util.SdDataAttributeConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ViKumar
 * 
 *
 */

@Slf4j
@Component
public class InstrumentInactivationServiceImpl implements InstrumentInactivationService {

	@Autowired
	private MergeAndPersistService mergeAndPersistService;

	@Autowired
	private DataContainerCloneService cloneService;

	@Override
	public void inactivateInstruments(DataContainer sdDataContainer, LockLevel inactivationLockLevel,
			String inactivationInitiatedByProgram, String dataSource) throws UdmBaseException {
		List<DataContainer> dbContainers = new ArrayList<>();
		dbContainers.add(sdDataContainer);
		DataContainer containerWithMinimumAttribute = cloneService.createMinimumAttributeInstrumentDC(sdDataContainer);
		DataValue<DomainType> statusDataValue = new DataValue<>();
		DomainType statusValueDomainType = new DomainType();
		statusValueDomainType.setNormalizedValue(DomainStatus.INACTIVE);
		statusDataValue.setValue(inactivationLockLevel, statusValueDomainType);

		containerWithMinimumAttribute.addAttributeValue(SdDataAttributeConstant.INS_STATUS, statusDataValue);
		
		List<DataContainer> childDataContainers = sdDataContainer.getChildDataContainers(DataLevel.SEC);
		if (null != childDataContainers) {
			containerWithMinimumAttribute.addDataContainers(childDataContainers, DataLevel.SEC);
		}
		containerWithMinimumAttribute
				.updateDataContainerContext(DataContainerContext.builder().withDataSource(dataSource)
						.withUpdateDateTime(LocalDateTime.now()).withProgram(inactivationInitiatedByProgram).build());
		log.debug(
				"mergeAndPersistService called to persist the inactive container and to send message to applicable components");
		
		mergeAndPersistService.mergeAndPersistSd(dataSource, containerWithMinimumAttribute, dbContainers, null);

	}


}
