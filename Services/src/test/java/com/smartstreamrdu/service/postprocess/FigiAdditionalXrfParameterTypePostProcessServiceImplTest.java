package com.smartstreamrdu.service.postprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.cache.CacheDataRetrieval;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.persistence.domain.autoconstants.SecurityAttrConstant;
import com.smartstreamrdu.service.domain.DomainLookupService;
import com.smartstreamrdu.service.domain.ServiceTestConfiguration;
import com.smartstreamrdu.service.normalized.NormalizedValueService;
import com.smartstreamrdu.service.util.MockUtil;
import com.smartstreamrdu.util.Constant.CrossRefConstants;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ServiceTestConfiguration.class })
public class FigiAdditionalXrfParameterTypePostProcessServiceImplTest {

	@Autowired
	private FigiAdditionalXrfParameterTypePostProcessServiceImpl postProcessor;

	private DataContainer secDataContainer1;

	private DataContainer secDataContainer2;
	
	private DataContainer secDataContainer3;

	private DataContainer insDataContainer;

	@Autowired
	private NormalizedValueService normalizedValueService;

	@Autowired
	private UdmSystemPropertiesCache systemCache;

	@Autowired
	private CacheDataRetrieval cacheDataRetrieval;
	
	@Autowired
	private DomainLookupService domainLookup;

	private static final DataAttribute instrumentId = DataAttributeFactory.getAttributeByNameAndLevel("_instrumentId",
			DataLevel.INS);
	private static final DataAttribute securityId = DataAttributeFactory.getAttributeByNameAndLevel("_securityId",
			DataLevel.SEC);
	private static final DataAttribute assetType = DataAttributeFactory
			.getAttributeByNameAndLevel(CrossRefConstants.INS_TYPE_CODE, DataLevel.INS);
	private static final DataAttribute dataSource = DataAttributeFactory.getAttributeByNameAndLevel("dataSource",
			DataLevel.Document);
	private static final DataAttribute tradeCurrencyCode = DataAttributeFactory
			.getAttributeByNameAndLevel("tradeCurrencyCode", DataLevel.SEC);
	private static final DataAttribute securityStatus = DataAttributeFactory
			.getAttributeByNameAndLevel("securityStatus", DataLevel.SEC);
    private static final DataAttribute rduSecurityType = DataAttributeFactory
            .getAttributeByNameAndLevel("rduSecurityType", DataLevel.SEC);	
	private static final DataAttribute instrumentStatus = DataAttributeFactory
			.getAttributeByNameAndLevel("instrumentStatus", DataLevel.INS);
	private static final DataAttribute isin = DataAttributeFactory.getAttributeByNameAndLevel(CrossRefConstants.ISIN,
			DataLevel.INS);
	private static final DataAttribute additionalAttribute = DataAttributeFactory
			.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC);
	private DataAttribute updDate = DataAttributeFactory.getAttributeByNameAndLevel("updDate", DataLevel.Document);
	private DataAttribute insDate = DataAttributeFactory.getAttributeByNameAndLevel("insDate", DataLevel.Document);
	private DataAttribute additionalParameterAttributeType = DataAttributeFactory
			.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC);
	
	private DataAttribute exchangeCodeDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.SEC);
	private DataAttribute bbgCompositeIdBbGlobalDataAttribute = DataAttributeFactory.getAttributeByNameAndLevel("bbgCompositeIdBbGlobal", DataLevel.SEC);
	
	DataValue<DomainType> exchangeCodeValue;
	DataValue<String> bbgCompositeIdBbGlobalValue;

	@Before
	public void setUp() throws Exception {
		
		DomainType exchCode = new DomainType();
		exchCode.setVal("XCME");
		
		exchangeCodeValue = new DataValue<>();
		exchangeCodeValue.setValue(LockLevel.FEED, exchCode);
		
		bbgCompositeIdBbGlobalValue = new DataValue<>();
		bbgCompositeIdBbGlobalValue.setValue(LockLevel.FEED, "ABC123");

		MockUtil.createNormalizedService(normalizedValueService);
		MockUtil.createUdmSystemPropertyCache(systemCache);
		Mockito.when(cacheDataRetrieval.getDataSourceDomainSourceFromCode("figi")).thenReturn("figi");

		secDataContainer1 = DataContainerTestUtil.getSecurityContainer();
		secDataContainer2 = DataContainerTestUtil.getSecurityContainer();
		secDataContainer3 = DataContainerTestUtil.getSecurityContainer();

		insDataContainer = DataContainerTestUtil.getInstrumentContainer();

		DataValue<DomainType> secStatusValue = new DataValue<>();
		DomainType secStatusDomainValue = new DomainType();
		secStatusDomainValue.setVal("1");
		secStatusValue.setValue(LockLevel.FEED, secStatusDomainValue);
		
        DataValue<DomainType> rduSecTypeRegValue = new DataValue<>();
        DomainType rduSecTypeRegDomainValue = new DomainType();
        rduSecTypeRegDomainValue.setNormalizedValue("Regular");
        rduSecTypeRegValue.setValue(LockLevel.FEED, rduSecTypeRegDomainValue);
        
        DataValue<DomainType> rduSecTypeTechValue = new DataValue<>();
        DomainType rduSecTypeTechDomainValue = new DomainType();
        rduSecTypeTechDomainValue.setNormalizedValue("Technical");
        rduSecTypeTechValue.setValue(LockLevel.FEED, rduSecTypeTechDomainValue);

		DataValue<String> securityIdValue = new DataValue<String>();
		securityIdValue.setValue(LockLevel.FEED, "a1b2c3");

		DataValue<DomainType> tradeCurrencyValue = new DataValue<>();
		DomainType tradeCurrencyDomainValue = new DomainType();
		tradeCurrencyDomainValue.setVal("USd");
		tradeCurrencyValue.setValue(LockLevel.FEED, tradeCurrencyDomainValue);

		secDataContainer1.addAttributeValue(securityStatus, secStatusValue);
		secDataContainer1.addAttributeValue(rduSecurityType, rduSecTypeRegValue);
		secDataContainer1.addAttributeValue(securityId, securityIdValue);
		secDataContainer1.addAttributeValue(tradeCurrencyCode, tradeCurrencyValue);
		secDataContainer2.addAttributeValue(securityStatus, secStatusValue);
        secDataContainer2.addAttributeValue(rduSecurityType, rduSecTypeRegValue);
		secDataContainer2.addAttributeValue(securityId, securityIdValue);
		secDataContainer2.addAttributeValue(tradeCurrencyCode, tradeCurrencyValue);
		secDataContainer3.addAttributeValue(securityStatus, secStatusValue);
        secDataContainer3.addAttributeValue(rduSecurityType, rduSecTypeTechValue);

		DataValue<String> instrumentIdValue = new DataValue<String>();
		instrumentIdValue.setValue(LockLevel.FEED, "1a2b3c");

		DataValue<LocalDateTime> updDateValue = new DataValue<>();
		updDateValue.setValue(LockLevel.RDU, LocalDateTime.now());

		DataValue<String> isinValue = new DataValue<>();
		isinValue.setValue(LockLevel.FEED, "US0126531013");

		DataValue<DomainType> assetTypeValue = new DataValue<DomainType>();
		DomainType domainType = new DomainType();
		domainType.setVal("CME");
		domainType.setVal2("EL");
		assetTypeValue.setValue(LockLevel.FEED, domainType);

		DataValue<DomainType> dataSourceValue = new DataValue<DomainType>();
		DomainType domainType1 = new DomainType();
		domainType1.setVal("figi");
		dataSourceValue.setValue(LockLevel.FEED, domainType1);

		insDataContainer.addDataContainer(secDataContainer1, DataLevel.SEC);
		insDataContainer.addDataContainer(secDataContainer2, DataLevel.SEC);
		insDataContainer.addDataContainer(secDataContainer3, DataLevel.SEC);
		insDataContainer.addAttributeValue(instrumentId, instrumentIdValue);
		insDataContainer.addAttributeValue(instrumentStatus, secStatusValue);
		insDataContainer.addAttributeValue(isin, isinValue);
		insDataContainer.addAttributeValue(updDate, updDateValue);
		insDataContainer.addAttributeValue(insDate, updDateValue);
		insDataContainer.addAttributeValue(assetType, assetTypeValue);
		insDataContainer.addAttributeValue(dataSource, dataSourceValue);
		
		DomainType specialCaseXrfAdditional1 = new DomainType();
		specialCaseXrfAdditional1.setVal("val1");
		specialCaseXrfAdditional1.setVal2("MM-Default");
		
		Mockito.when(domainLookup.getPatternMatchNormalizedValueForDomainValue(specialCaseXrfAdditional1,null,"xrfAdditionalTypes")).thenReturn("Default");
		
		

	}

	@Test
	public void testIsServiceApplicable_positive() throws UdmTechnicalException {
		boolean isApplicable = postProcessor.isServiceApplicable("figi", new ArrayList<>());
		Assert.assertTrue(isApplicable);
	}

	@Test
	public void testIsServiceApplicable_negetive() throws UdmTechnicalException {
		boolean isApplicable = postProcessor.isServiceApplicable("trdse", new ArrayList<>());
		Assert.assertTrue(!isApplicable);
	}

	@Test
	public void testPostProcessContainersWhenFigiSpecialCaseKeysOnly() throws UdmTechnicalException {

		List<DataValue<DomainType>> listOfAdditionalAttr = new ArrayList<DataValue<DomainType>>();

		DataValue<DomainType> additionalAttr1 = new DataValue<>();
		DataValue<DomainType> additionalAttr2 = new DataValue<>();
		DomainType specialCaseXrfAdditional1 = new DomainType();
		specialCaseXrfAdditional1.setVal("val1");
		specialCaseXrfAdditional1.setVal2("MM");

		DomainType specialCaseXrfAdditional2 = new DomainType();
		specialCaseXrfAdditional2.setVal("val1");
		specialCaseXrfAdditional2.setVal2("MM");

		listOfAdditionalAttr.add(additionalAttr1);
		listOfAdditionalAttr.add(additionalAttr2);

		List<DomainType> listOfDomainTypeGlobal = new ArrayList<DomainType>();
		listOfDomainTypeGlobal.add(specialCaseXrfAdditional1);
		listOfDomainTypeGlobal.add(specialCaseXrfAdditional2);
		setXrfAdditionalAttributeForSecurity(listOfAdditionalAttr, listOfDomainTypeGlobal,
				insDataContainer.getAllChildDataContainers());

		List<DataContainer> dataContainers = new ArrayList<DataContainer>();
		dataContainers.add(insDataContainer);

		postProcessor.postProcessContainers("figi", dataContainers);

		HashSet<String> resultSet = new HashSet<String>();
		for (DataContainer dataContainer : dataContainers) {
			List<DataContainer> childDataContainers = dataContainer.getChildDataContainers(DataLevel.SEC);
			List<DomainType> listOfDomainType = getXrfAdditionalAttrValueFromDataCOntainer(childDataContainers,
					LockLevel.FEED);
			for (DomainType domainType : listOfDomainType) {
				Serializable normalizedValueForDomainValue = normalizedValueService.getNormalizedValueForDomainValue(
						DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC),
						domainType, "figi");
				resultSet.add((String) normalizedValueForDomainValue);
			}
			assertNull(childDataContainers.get(2).getHighestPriorityValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE));
		}
		Assert.assertTrue((resultSet.contains("Default")) && (resultSet.size() == 1));
		
	}

	@Test
	public void testPostProcessContainersWhenFigiSpecialCaseKeysWithDefault() throws UdmTechnicalException {

		List<DataValue<DomainType>> listOfAdditionalAttr = new ArrayList<DataValue<DomainType>>();

		DataValue<DomainType> additionalAttr1 = new DataValue<>();
		DataValue<DomainType> additionalAttr2 = new DataValue<>();
		DomainType specialCaseXrfAdditional1 = new DomainType();
		specialCaseXrfAdditional1.setVal("Default");
		specialCaseXrfAdditional1.setVal2("Default");

		DomainType specialCaseXrfAdditional2 = new DomainType();
		specialCaseXrfAdditional2.setVal(".*C$");
		specialCaseXrfAdditional2.setVal2("AF");

		listOfAdditionalAttr.add(additionalAttr1);
		listOfAdditionalAttr.add(additionalAttr2);

		List<DomainType> listOfDomainTypeGlobal = new ArrayList<DomainType>();
		listOfDomainTypeGlobal.add(specialCaseXrfAdditional1);
		listOfDomainTypeGlobal.add(specialCaseXrfAdditional2);
		setXrfAdditionalAttributeForSecurity(listOfAdditionalAttr, listOfDomainTypeGlobal,
				insDataContainer.getAllChildDataContainers());

		List<DataContainer> dataContainers = new ArrayList<DataContainer>();
		dataContainers.add(insDataContainer);

		postProcessor.postProcessContainers("figi", dataContainers);

		HashSet<String> resultSet = new HashSet<String>();
		for (DataContainer dataContainer : dataContainers) {
			List<DataContainer> childDataContainers = dataContainer.getChildDataContainers(DataLevel.SEC);
			List<DomainType> listOfDomainType = getXrfAdditionalAttrValueFromDataCOntainer(childDataContainers,
					LockLevel.FEED);
			for (DomainType domainType : listOfDomainType) {
				Serializable normalizedValueForDomainValue = normalizedValueService.getNormalizedValueForDomainValue(
						DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.SEC),
						domainType, "figi");
				resultSet.add((String) normalizedValueForDomainValue);
			}
			assertNull(childDataContainers.get(2).getHighestPriorityValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE));
		}
		Assert.assertTrue((resultSet.contains("Default")) && (resultSet.contains("Security (Composite)"))
				&& (resultSet.size() == 2));
	}
	
	@Test
	public void testPostProcessContainerWhenNormalizedValue() throws UdmTechnicalException {
		List<DataValue<DomainType>> listOfAdditionalAttr = new ArrayList<DataValue<DomainType>>();

		DataValue<DomainType> additionalAttr1 = new DataValue<>();
		DataValue<DomainType> additionalAttr2 = new DataValue<>();
		DomainType specialCaseXrfAdditional1 = new DomainType();
		specialCaseXrfAdditional1.setNormalizedValue("Default");

		DomainType specialCaseXrfAdditional2 = new DomainType();
		specialCaseXrfAdditional2.setNormalizedValue("Short Sell");

		listOfAdditionalAttr.add(additionalAttr1);
		listOfAdditionalAttr.add(additionalAttr2);

		List<DomainType> listOfDomainTypeGlobal = new ArrayList<DomainType>();
		listOfDomainTypeGlobal.add(specialCaseXrfAdditional1);
		listOfDomainTypeGlobal.add(specialCaseXrfAdditional2);
		setXrfAdditionalAttributeForSecurity(listOfAdditionalAttr, listOfDomainTypeGlobal,
				insDataContainer.getAllChildDataContainers());

		List<DataContainer> dataContainers = new ArrayList<DataContainer>();
		dataContainers.add(insDataContainer);

		postProcessor.postProcessContainers("figi", dataContainers);

		HashSet<String> resultSet = new HashSet<String>();
		for (DataContainer dataContainer : dataContainers) {
			List<DataContainer> childDataContainers = dataContainer.getChildDataContainers(DataLevel.SEC);
			List<DomainType> listOfDomainType = getXrfAdditionalAttrValueFromDataCOntainer(childDataContainers,
					LockLevel.FEED);
			for (DomainType domainType : listOfDomainType) {
				resultSet.add(domainType.getNormalizedValue());
			}
			assertNull(childDataContainers.get(2).getHighestPriorityValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE));
		}
		Assert.assertTrue(resultSet.contains("Short Sell") && resultSet.contains("Default") && resultSet.size() == 2);
		
	}
	
	@Test
	public void testPostProcessContainers_OpsLockOnAdditioanlXrfParamater() throws UdmTechnicalException {
		List<DataContainer> allChildDataContainers = insDataContainer.getAllChildDataContainers();
		DataContainer figiSecurityContainer1=allChildDataContainers.get(0);
		DataValue<DomainType> rduvalue = new DataValue<>();
		rduvalue.setValue(LockLevel.RDU, new DomainType(null,null,"1 Day Repo"));
		figiSecurityContainer1.addAttributeValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE, rduvalue);
		
		DataContainer figiSecurityContainer2=allChildDataContainers.get(1);
		DataValue<DomainType> feedValue = new DataValue<>();
		feedValue.setValue(LockLevel.FEED, new DomainType("ABC","PQR",null,"xrfAdditionalTypes"));
		figiSecurityContainer2.addAttributeValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE, feedValue);
		
		
		postProcessor.postProcess("figi", List.of(insDataContainer));
		
		assertEquals(figiSecurityContainer1.getHighestPriorityValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE), new DomainType(null,null,"1 Day Repo"));
		assertEquals(figiSecurityContainer2.getHighestPriorityValue(SecurityAttrConstant.ADDITIONAL_XRF_PARAMETER_TYPE), new DomainType("ABC","PQR",null,"xrfAdditionalTypes"));
	}

	private List<DomainType> getXrfAdditionalAttrValueFromDataCOntainer(List<DataContainer> childDataContainers,
			LockLevel lockLevel) {
		List<DomainType> listOfDomainType = new ArrayList<DomainType>();
		if (null == childDataContainers) {
			childDataContainers = new ArrayList<DataContainer>();
		}
        for (DataContainer dataContainer : childDataContainers) {
            DataRow insDataRow = dataContainer.getDataRow();
            Map<DataAttribute, DataValue<Serializable>> rowData = insDataRow.getRowData();
            if (rowData.get(additionalParameterAttributeType) != null) {
                Serializable valueRaw = rowData.get(additionalParameterAttributeType).getValue(lockLevel);
                listOfDomainType.add((DomainType) valueRaw);
            }
        }
		return listOfDomainType;
	}

	private void setXrfAdditionalAttributeForSecurity(List<DataValue<DomainType>> listDataValue,
			List<DomainType> listOfDomainType, List<DataContainer> securityList) {
		if ((listDataValue.size()) > 0 && (securityList.size() > 0)) {
			for (int i = 0; i < listDataValue.size(); i++) {
				listDataValue.get(i).setValue(LockLevel.FEED, listOfDomainType.get(i));
				listDataValue.get(i).setValue(LockLevel.RDU, listOfDomainType.get(i));
				securityList.get(i).addAttributeValue(additionalAttribute, listDataValue.get(i));
				securityList.get(i).addAttributeValue(exchangeCodeDataAttribute, exchangeCodeValue);
				securityList.get(i).addAttributeValue(bbgCompositeIdBbGlobalDataAttribute, bbgCompositeIdBbGlobalValue);
			}
		}
	}

}
