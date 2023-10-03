/**
 * 
 */
package com.smartstreamrdu.listener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.listener.ListenerService;
import com.smartstreamrdu.util.Constant;

/**
 * @author Padgaonkar
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class ListenerServiceImplTest {
	
	
	@Autowired
	ListenerService  service;
	

	private DataContainer getDbDataContainer()
	{
		DataContainer container = DataContainerTestUtil.getDataContainer(DataLevel.XRF_INS);
		DataValue<String> isinVal = new DataValue<>();
		isinVal.setValue(LockLevel.RDU, "AE009A2F5S55");

		DataValue<DomainType> insStatus = new DataValue<>();
		insStatus.setValue(LockLevel.RDU, new DomainType(null, null, "A"));
		
		DataValue<DomainType> dataSource = new DataValue<>();
		dataSource.setValue(LockLevel.FEED, new DomainType("trdse", null, "A"));
		
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(Constant.CrossRefConstants.XRF_ISIN, DataLevel.XRF_INS),isinVal);
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(Constant.CrossRefConstants.XR_INS_STATUS, DataLevel.XRF_INS), insStatus);
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document), dataSource);
		return container;
		
	}
	
	private DataContainer getFeedDataContainer()
	{
		DataContainer container = DataContainerTestUtil.getDataContainer(DataLevel.XRF_INS);
		DataValue<String> isinVal = new DataValue<>();
		isinVal.setValue(LockLevel.FEED, "AE009A2F5S55");

		DataValue<DomainType> insStatus = new DataValue<>();
		insStatus.setValue(LockLevel.FEED, new DomainType(null, null, "A"));
		
		DataValue<DomainType> dataSource = new DataValue<>();
		dataSource.setValue(LockLevel.FEED, new DomainType("trdse", null, "A"));
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(Constant.CrossRefConstants.XRF_ISIN, DataLevel.XRF_INS),isinVal);
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel(Constant.CrossRefConstants.XR_INS_STATUS, DataLevel.XRF_INS), insStatus);
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("dataSource", DataLevel.Document), dataSource);
		return container;
		
	}
	
	@Test
	public void setUp() throws Exception {
		
		service.newdataContainer(getDbDataContainer());
		service.dataContainerMerge("trdse", getFeedDataContainer(), getDbDataContainer());
		service.mergeComplete("trdse", getFeedDataContainer(), getDbDataContainer());
	}

	
}
