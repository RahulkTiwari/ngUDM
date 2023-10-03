package com.smartstreamrdu.listener;
import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.events.InactiveBean;
import com.smartstreamrdu.persistence.domain.autoconstants.EnDataAttrConstant;
import com.smartstreamrdu.persistence.domain.autoconstants.IvoInstrumentAttrConstant;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.service.events.MergeCompleteChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.NewDataContainerChangeEventListenerInputCreationContext;
import com.smartstreamrdu.service.events.NewDataContainerEvent;
import com.smartstreamrdu.service.events.UpdateDataContainerEvent;
import com.smartstreamrdu.util.Constant.ListenerConstants;
import com.smartstreamrdu.util.Constant.OpenFigiContants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class DataContainerEventListnerTest {

	InactiveBean inactiveBean = null;

	private static final DataAttribute sedol = DataAttributeFactory.getAttributeByNameAndLevel(OpenFigiContants.SEDOL, DataLevel.SEC);
	private static final DataAttribute cusip = DataAttributeFactory.getAttributeByNameAndLevel(OpenFigiContants.CUSIP, DataLevel.INS);
	private static final DataAttribute instrumentId = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.instrumentId, DataLevel.INS);
	private static final DataAttribute securityId = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.securityId, DataLevel.SEC);
	private static final DataAttribute dataSource = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.datasource, DataLevel.Document);
	private static final DataAttribute exchangeCode = DataAttributeFactory.getAttributeByNameAndLevel(OpenFigiContants.EX_CODE, DataLevel.SEC);
	private static final DataAttribute insertUser = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertUser,DataLevel.Document);
	private static final DataAttribute insertDate = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertDate,DataLevel.Document);
	private static final DataAttribute updateDate = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateDate, DataLevel.Document);
	private static final DataAttribute updateUser = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.updateUser, DataLevel.Document);
	private static final DataAttribute IVO_INS_DATE = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertDate, DataLevel.IVO_DOC);
	private static final DataAttribute IVO_INS_USER = DataAttributeFactory.getAttributeByNameAndLevel(ListenerConstants.insertUser, DataLevel.IVO_DOC);


	private DataContainer secDataContainer;
	private DataContainer insDataContainer;
	private DataContainer insIvoDataContainer;
	private DataContainer enDataContainer;

	
	@Autowired
	private NewDataContainerEvent newDataContainerEventListner;
	
	@Autowired
	private UpdateDataContainerEvent updateDataContainerEventListner;

	@Before
	public void setUp() throws Exception {
		
		DataContainerContext dataContainerContext= DataContainerContext.builder().withProgram("NG-UDL").
				withUpdateDateTime(LocalDateTime.now()).build();
		insIvoDataContainer=	new DataContainer(DataLevel.IVO_INS, dataContainerContext);
		enDataContainer= new DataContainer(DataLevel.EN, dataContainerContext);

		insDataContainer=new DataContainer(DataLevel.INS, dataContainerContext);
		insDataContainer.addAudit();
		secDataContainer=new DataContainer(DataLevel.SEC, dataContainerContext);
		inactiveBean = new InactiveBean();
		
		DataValue<String> sedolDataValue = new DataValue<String>();
		sedolDataValue.setValue(LockLevel.FEED, "ABC");
		secDataContainer.addAttributeValue(sedol, sedolDataValue);
		
		DataValue<String> securityIdValue = new DataValue<String>();
		securityIdValue.setValue(LockLevel.FEED, "a1b2c3");
		secDataContainer.addAttributeValue(securityId, securityIdValue);
		
		DataValue<DomainType> exchangeCodeValue = new DataValue<DomainType>();
		DomainType exchangeCodeDomainType = new DomainType();
		exchangeCodeDomainType.setVal("NYS");
		exchangeCodeValue.setValue(LockLevel.FEED, exchangeCodeDomainType);
		secDataContainer.addAttributeValue(exchangeCode, exchangeCodeValue);
		secDataContainer.addAudit();
		DataValue<String> cusipDataValue = new DataValue<String>();
		cusipDataValue.setValue(LockLevel.FEED, "XYZ");
		insDataContainer.addAttributeValue(cusip, cusipDataValue);
		
		DataValue<String> instrumentIdValue = new DataValue<String>();
		instrumentIdValue.setValue(LockLevel.FEED, "1a2b3c");
		insDataContainer.addAttributeValue(instrumentId, instrumentIdValue);
		
		DataValue<DomainType> dataSourceValue = new DataValue<DomainType>();
		DomainType domainType1 = new DomainType();
		domainType1.setVal("trdse");
		dataSourceValue.setValue(LockLevel.FEED, domainType1);
		
		insDataContainer.addAttributeValue(dataSource, dataSourceValue);
		insDataContainer.addDataContainer(secDataContainer, DataLevel.SEC);
			
		inactiveBean.setDbContainer(insDataContainer);
		

		DataValue<String> instrumentIdValue1 = new DataValue<String>();
		instrumentIdValue1.setValue(LockLevel.FEED, "1a2b3c");
		
		insIvoDataContainer.addAudit();
		insIvoDataContainer.addAttributeValue(IvoInstrumentAttrConstant._INSTRUMENT_ID, instrumentIdValue1);
		
		
		DataValue<String> eventValue = new DataValue<String>();
		eventValue.setValue(LockLevel.FEED, "1a2b3c");
		
		enDataContainer.addAudit();
		enDataContainer.addAttributeValue(EnDataAttrConstant.EVENT_SOURCE_UNIQUE_ID, eventValue);

	}

	@Test
	public void testNewDataContainerEventListner() {
		newDataContainerEventListner.propogateEvent(insDataContainer);
		NewDataContainerChangeEventListenerInputCreationContext context = new NewDataContainerChangeEventListenerInputCreationContext();
		context.setDataContainer(insDataContainer);
		DataContainer container1  = newDataContainerEventListner.createInput(context);
		Assert.assertNotNull( container1.getAttributeValueAtLevel(LockLevel.RDU, insertDate));
		Assert.assertEquals("udl", container1.getAttributeValueAtLevel(LockLevel.RDU, insertUser));	
	}
	
	@Test
	public void testNewDataContainerEventListnerForIVO() {		
		newDataContainerEventListner.propogateEvent(insIvoDataContainer);
		NewDataContainerChangeEventListenerInputCreationContext context = new NewDataContainerChangeEventListenerInputCreationContext();
		context.setDataContainer(insIvoDataContainer);
		DataContainer container1  = newDataContainerEventListner.createInput(context);
		Assert.assertNotNull( container1.getAttributeValueAtLevel(LockLevel.RDU, IVO_INS_DATE));
		Assert.assertEquals("udl", container1.getAttributeValueAtLevel(LockLevel.RDU, IVO_INS_USER));	
	}
	
	@Test
	public void testNewDataContainerEventListnerForEN() {		
		newDataContainerEventListner.propogateEvent(enDataContainer);
		NewDataContainerChangeEventListenerInputCreationContext context = new NewDataContainerChangeEventListenerInputCreationContext();
		context.setDataContainer(enDataContainer);
		DataContainer container1  = newDataContainerEventListner.createInput(context);
		Assert.assertNotNull( container1.getAttributeValueAtLevel(LockLevel.RDU, EnDataAttrConstant.INS_DATE));
		Assert.assertEquals("udl", container1.getAttributeValueAtLevel(LockLevel.RDU, EnDataAttrConstant.INS_USER));	
	}
	@Test
	public void testUpdateDataContainerEventListner() {
		insDataContainer.setNew(false);
		insDataContainer.setHasChanged(true);
		updateDataContainerEventListner.propogateEvent(inactiveBean);
		MergeCompleteChangeEventListenerInputCreationContext context = new MergeCompleteChangeEventListenerInputCreationContext();
		context.setDataContainer(insDataContainer);
		context.setDataSource(ListenerConstants.datasource);
		InactiveBean inactiveBean1 = updateDataContainerEventListner.createInput(context);
		DataContainer container1 = inactiveBean1.getDbContainer();
		Assert.assertNotNull( container1.getAttributeValueAtLevel(LockLevel.RDU, updateDate));
		Assert.assertEquals("udl", container1.getAttributeValueAtLevel(LockLevel.RDU, updateUser));
	}

}
