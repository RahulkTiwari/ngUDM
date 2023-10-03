package com.smartstreamrdu.service.merging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerTestUtil;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.RduLockLevelInfo;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.util.Constant;

public class CrossRefDataContainers {
	
	private static final String INSTRUMENT_XRFDATA_SOURCE = Constant.CrossRefConstants.INS_DATASOURCE;
	private static final String TRADE_CRR = Constant.CrossRefConstants.TRADE_CURR;
	private static final String XRF_TRADE_CURR = Constant.CrossRefConstants.XRF_TRADE_CURR;
	
	public DataContainer getDataContainerWithTwoXRSec() {

		DataContainer container = DataContainerTestUtil.getDataContainer(DataLevel.XRF_INS);

		DataValue<String> instrumentId1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		instrumentId1.setValue(LockLevel.RDU, "DummyI3", rduLockLevelInfo);
		
		
		DataValue<DomainType> status = new DataValue<>();
		DomainType iStat = new DomainType();
		iStat.setNormalizedValue(Constant.DomainStatus.ACTIVE);
		status.setValue(LockLevel.RDU, iStat, rduLockLevelInfo);

		DataValue<String> isinVal1 = new DataValue<>();
		isinVal1.setValue(LockLevel.RDU, "DummyIsin", rduLockLevelInfo);

		DataValue<String> isinVal2 = new DataValue<>();
		isinVal2.setValue(LockLevel.RDU, "DummyIsin", rduLockLevelInfo);

		DataValue<String> cusip1 = new DataValue<>();
		cusip1.setValue(LockLevel.RDU, "DummyCusip", rduLockLevelInfo);

		DataValue<String> cusip2 = new DataValue<>();
		cusip2.setValue(LockLevel.RDU, "DummyCusip2", rduLockLevelInfo);

		DataValue<DomainType> assetType1 = new DataValue<>();
		DomainType as1 = new DomainType();
		as1.setNormalizedValue("0");
		assetType1.setValue(LockLevel.RDU, as1, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource1 = new DataValue<>();
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DummyDS9");
		instrumentXrfdataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource2 = new DataValue<>();
		DomainType dataSource2 = new DomainType();
		dataSource2.setNormalizedValue("DummyDS10");
		instrumentXrfdataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus1 = new DataValue<>();
		instrumentXrfLinkStatus1.setValue(LockLevel.RDU,new DomainType("A",null,null), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate1 = new DataValue<>();
		instrumentLinkStartDate1.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);


		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType("A",null,null), rduLockLevelInfo);


		DataValue<ReferenceId> instrumentRef1 = new DataValue<>();
		instrumentRef1.setValue(LockLevel.RDU, new ReferenceId("DummyI9", "DummyD9"), rduLockLevelInfo);

		DataValue<ReferenceId> instrumentRef2 = new DataValue<>();
		instrumentRef2.setValue(LockLevel.RDU, new ReferenceId("DummyI91", "DummyD10"), rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("USD");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("USD2");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("DummyEx");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("DummyEx2");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency3 = new DataValue<>();
		DomainType tc3 = new DomainType();
		tc3.setNormalizedValue("USD3");
		tradeCurrency3.setValue(LockLevel.RDU, tc3, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency4 = new DataValue<>();
		DomainType tc4 = new DomainType();
		tc4.setNormalizedValue("USD4");
		tradeCurrency4.setValue(LockLevel.RDU, tc4, rduLockLevelInfo);

		DataValue<DomainType> additionalXrfParameter1 = new DataValue<>();
		additionalXrfParameter1.setValue(LockLevel.RDU, new DomainType("AA1"), rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC1", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, UUID.randomUUID().toString(), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("DummyS9", "DummyD9"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("DummyS91", "DummyD10"), rduLockLevelInfo);

		DataAttribute xrfInsLink = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);

		DataRow link1 = new DataRow(xrfInsLink);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("cusip", DataLevel.XRF_INS), cusip1);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel(INSTRUMENT_XRFDATA_SOURCE, DataLevel.XRF_INS),
				instrumentXrfdataSource1);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS),
				instrumentXrfLinkStatus1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS),
				instrumentRef1);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkInitialDate", DataLevel.XRF_INS),
				instrumentLinkStartDate1);

		DataRow link2 = new DataRow(xrfInsLink);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("cusip", DataLevel.XRF_INS), cusip2);
		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel(INSTRUMENT_XRFDATA_SOURCE, DataLevel.XRF_INS),
				instrumentXrfdataSource2);
		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS),
				instrumentXrfLinkStatus1);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS),
				instrumentRef2);
		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkInitialDate", DataLevel.XRF_INS),
				instrumentLinkStartDate1);

		DataAttribute xrfInsLinks = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);

		ArrayList<DataRow> linkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> valueList = new DataValue<>();
		valueList.setValue(LockLevel.RDU, linkList, rduLockLevelInfo);

		linkList.add(link1);
		linkList.add(link2);

		DataRow links = new DataRow(xrfInsLinks, valueList);

		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfIsin", DataLevel.XRF_INS),
				isinVal1);

	
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfCusip", DataLevel.XRF_INS),
				cusip1);
		
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfInstrumentStatus", DataLevel.XRF_INS), status);

		container.addAttributeValue(xrfInsLinks, links);

		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);
		container.addDataContainer(secContainer, DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		sedol1.setValue(LockLevel.RDU, "DummySedol", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "DummySedol2", rduLockLevelInfo);

		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		DataRow seclink1 = new DataRow(xrfSecLink);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.XRF_SEC),
				additionalXrfParameter1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId1);

		DataRow seclink2 = new DataRow(xrfSecLink);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				instrumentXrfdataSource2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.XRF_SEC),
				additionalXrfParameter1);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId2);

		DataAttribute xrfsecLinks2 = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList = new ArrayList<>();
		seclinkList.add(seclink1);
		seclinkList.add(seclink2);

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, rduLockLevelInfo);

		DataRow seclinks = new DataRow(xrfsecLinks2, secvalueList);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfAdditionalXrfParameterType", DataLevel.XRF_SEC),
				additionalXrfParameter1);
	
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSecurityStatus", DataLevel.XRF_SEC), status);

		secContainer.addAttributeValue(xrfsecLinks2, seclinks);

		DataContainer secContainer1 = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);
		container.addDataContainer(secContainer1, DataLevel.XRF_SEC);

		DataValue<ReferenceId> _securityXrfRefId3 = new DataValue<>();
		_securityXrfRefId3.setValue(LockLevel.RDU, new ReferenceId("DummyS93", "DummyD9"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId4 = new DataValue<>();
		_securityXrfRefId4.setValue(LockLevel.RDU, new ReferenceId("DummyS94", "DummyD10"), rduLockLevelInfo);

		DataValue<String> sedol3 = new DataValue<>();
		sedol3.setValue(LockLevel.RDU, "DummySedo3", rduLockLevelInfo);

		DataValue<String> sedol4 = new DataValue<>();
		sedol4.setValue(LockLevel.RDU, "DummySedol4", rduLockLevelInfo);

		DataAttribute xrfSecLink1 = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		DataRow seclink3 = new DataRow(xrfSecLink1);
		seclink3.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol3);
		seclink3.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				instrumentXrfdataSource2);
		seclink3.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink3.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency3);
		seclink3.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		seclink3.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.XRF_SEC),
				additionalXrfParameter1);
		seclink3.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		seclink3.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId3);

		DataRow seclink4 = new DataRow(xrfSecLink1);
		seclink4.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol4);
		seclink4.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource1);
		seclink4.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink4.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency4);
		seclink4.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode2);
		seclink4.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("additionalXrfParameterType", DataLevel.XRF_SEC),
				additionalXrfParameter1);
		seclink4.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		seclink4.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId4);

		DataAttribute xrfsecLinks5 = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList3 = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList3 = new DataValue<>();
		secvalueList3.setValue(LockLevel.RDU, seclinkList3, rduLockLevelInfo);

		seclinkList3.add(seclink3);
		seclinkList3.add(seclink4);
		DataRow seclinks3 = new DataRow(xrfsecLinks5, secvalueList3);

		secContainer1.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol3);
		secContainer1.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer1.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
		secContainer1.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfAdditionalXrfParameterType", DataLevel.XRF_SEC),
				additionalXrfParameter1);
		secContainer1.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSecurityStatus", DataLevel.XRF_SEC), status);

		secContainer1.addAttributeValue(xrfsecLinks5, seclinks3);

		return container;

	
	}

	public DataContainer getDataContainer1() {

		DataContainer container = DataContainerTestUtil.getDataContainer(DataLevel.XRF_INS);

		DataValue<String> instrumentId1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		instrumentId1.setValue(LockLevel.RDU, "I101", rduLockLevelInfo);
		
		DataValue<DomainType> insStatus = new DataValue<>();
		DomainType status= new DomainType();
		status.setNormalizedValue("A");
		insStatus.setValue(LockLevel.RDU,status, rduLockLevelInfo);

		DataValue<String> instrumentId2 = new DataValue<>();
		instrumentId2.setValue(LockLevel.RDU, "I102", rduLockLevelInfo);

		DataValue<String> isinVal = new DataValue<>();
		isinVal.setValue(LockLevel.RDU, "isin101", rduLockLevelInfo);

		DataValue<String> isinVal2 = new DataValue<>();
		isinVal2.setValue(LockLevel.RDU, "isin102", rduLockLevelInfo);

		DataValue<String> cusip1 = new DataValue<>();
		cusip1.setValue(LockLevel.RDU, "cusip101", rduLockLevelInfo);

		DataValue<String> cusip2 = new DataValue<>();
		cusip2.setValue(LockLevel.RDU, "cusip102", rduLockLevelInfo);

		DataValue<DomainType> assetType1 = new DataValue<>();
		DomainType as1 = new DomainType();
		as1.setNormalizedValue("asset101");
		assetType1.setValue(LockLevel.RDU, as1, rduLockLevelInfo);

		DataValue<DomainType> assetType2 = new DataValue<>();
		DomainType as2 = new DomainType();
		as2.setNormalizedValue("asset102");
		assetType2.setValue(LockLevel.RDU, as2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource1 = new DataValue<>();
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS101");
		instrumentXrfdataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource2 = new DataValue<>();
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS101");
		instrumentXrfdataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus1 = new DataValue<>();
		instrumentXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType("A",null,null), rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus2 = new DataValue<>();
		instrumentXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("A",null,null), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate1 = new DataValue<>();
		instrumentLinkStartDate1.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate2 = new DataValue<>();
		instrumentLinkStartDate2.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);


		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType("A",null,null), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I",null,null), rduLockLevelInfo);


		DataValue<ReferenceId> instrumentRef1 = new DataValue<>();
		instrumentRef1.setValue(LockLevel.RDU, new ReferenceId("I101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> instrumentRef2 = new DataValue<>();
		instrumentRef2.setValue(LockLevel.RDU, new ReferenceId("I102", "D102"), rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC101");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC102");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC101");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC102");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC101", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC102", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS101", rduLockLevelInfo);

		DataValue<String> _instrument = new DataValue<>();
		_instrument.setValue(LockLevel.RDU, "XRI101", rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S102", "D102"), rduLockLevelInfo);

		DataAttribute xrfInsLink = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);
		DataRow link2 = new DataRow(xrfInsLink);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("cusip", DataLevel.XRF_INS), cusip2);

		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel(INSTRUMENT_XRFDATA_SOURCE, DataLevel.XRF_INS),
				instrumentXrfdataSource2);
		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS),
				instrumentXrfLinkStatus2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS),
				instrumentRef2);
		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkInitialDate", DataLevel.XRF_INS),
				instrumentLinkStartDate2);

		DataRow link1 = new DataRow(xrfInsLink);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal2);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel(INSTRUMENT_XRFDATA_SOURCE, DataLevel.XRF_INS),
				instrumentXrfdataSource1);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS),
				instrumentXrfLinkStatus1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS),
				instrumentRef1);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkInitialDate", DataLevel.XRF_INS),
				instrumentLinkStartDate1);

		DataAttribute xrfInsLinks = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);

		ArrayList<DataRow> linkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> valueList = new DataValue<>();
		valueList.setValue(LockLevel.RDU, linkList, rduLockLevelInfo);

		linkList.add(link1);
		linkList.add(link2);

		DataRow links = new DataRow(xrfInsLinks, valueList);

		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfIsin", DataLevel.XRF_INS),
				isinVal);
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfInstrumentStatus", DataLevel.XRF_INS),
				insStatus);

	
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfCusip", DataLevel.XRF_INS),
				cusip1);
		

		container.addAttributeValue(xrfInsLinks, links);

		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);
		container.addDataContainer(secContainer, DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		sedol1.setValue(LockLevel.RDU, "sedol101", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "sedol102", rduLockLevelInfo);

		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);
		DataRow seclink2 = new DataRow(xrfSecLink);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId2);

		DataRow seclink1 = new DataRow(xrfSecLink);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId1);

		DataAttribute xrfsecLinks = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, rduLockLevelInfo);

		seclinkList.add(seclink1);
		seclinkList.add(seclink2);
		DataRow seclinks = new DataRow(xrfsecLinks, secvalueList);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSecurityStatus", DataLevel.XRF_SEC),
				insStatus);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
		
		secContainer.addAttributeValue(xrfsecLinks, seclinks);

		return container;

	}

	public DataContainer getDataContainer2() {
		

		DataContainer container = DataContainerTestUtil.getDataContainer(DataLevel.XRF_INS);

		DataValue<String> instrumentId1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		instrumentId1.setValue(LockLevel.RDU, "I201", rduLockLevelInfo);
		
		DataValue<DomainType> insStatus = new DataValue<>();
		DomainType status= new DomainType();
		status.setNormalizedValue("A");
		insStatus.setValue(LockLevel.RDU,status, rduLockLevelInfo);

		DataValue<String> instrumentId2 = new DataValue<>();
		instrumentId2.setValue(LockLevel.RDU, "I202", rduLockLevelInfo);

		DataValue<String> isinVal = new DataValue<>();
		isinVal.setValue(LockLevel.RDU, "isin201", rduLockLevelInfo);

		DataValue<String> isinVal2 = new DataValue<>();
		isinVal2.setValue(LockLevel.RDU, "isin202", rduLockLevelInfo);

		DataValue<String> cusip1 = new DataValue<>();
		cusip1.setValue(LockLevel.RDU, "cusip201", rduLockLevelInfo);

		DataValue<String> cusip2 = new DataValue<>();
		cusip2.setValue(LockLevel.RDU, "cusip202", rduLockLevelInfo);

		DataValue<DomainType> assetType1 = new DataValue<>();
		DomainType as1 = new DomainType();
		as1.setNormalizedValue("asset201");
		assetType1.setValue(LockLevel.RDU, as1, rduLockLevelInfo);

		DataValue<DomainType> assetType2 = new DataValue<>();
		DomainType as2 = new DomainType();
		as2.setNormalizedValue("asset202");
		assetType2.setValue(LockLevel.RDU, as2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource1 = new DataValue<>();
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS201");
		instrumentXrfdataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource2 = new DataValue<>();
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS201");
		instrumentXrfdataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus1 = new DataValue<>();
		instrumentXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus2 = new DataValue<>();
		instrumentXrfLinkStatus2.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate1 = new DataValue<>();
		instrumentLinkStartDate1.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate2 = new DataValue<>();
		instrumentLinkStartDate2.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I"), rduLockLevelInfo);

		DataValue<ReferenceId> instrumentRef1 = new DataValue<>();
		instrumentRef1.setValue(LockLevel.RDU, new ReferenceId("I101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> instrumentRef2 = new DataValue<>();
		instrumentRef2.setValue(LockLevel.RDU, new ReferenceId("I202", "D202"), rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC201");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC202");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC201");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC202");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC201", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC202", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS201", rduLockLevelInfo);

		DataValue<String> _instrument = new DataValue<>();
		_instrument.setValue(LockLevel.RDU, "XRI201", rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S202", "D202"), rduLockLevelInfo);

		DataAttribute xrfInsLink = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);
		DataRow link2 = new DataRow(xrfInsLink);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("cusip", DataLevel.XRF_INS), cusip2);

		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel(INSTRUMENT_XRFDATA_SOURCE, DataLevel.XRF_INS),
				instrumentXrfdataSource2);
		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS),
				instrumentXrfLinkStatus2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS),
				instrumentRef2);
		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkInitialDate", DataLevel.XRF_INS),
				instrumentLinkStartDate2);

		DataRow link1 = new DataRow(xrfInsLink);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal2);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel(INSTRUMENT_XRFDATA_SOURCE, DataLevel.XRF_INS),
				instrumentXrfdataSource1);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS),
				instrumentXrfLinkStatus1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS),
				instrumentRef1);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkInitialDate", DataLevel.XRF_INS),
				instrumentLinkStartDate1);

		DataAttribute xrfInsLinks = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);

		ArrayList<DataRow> linkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> valueList = new DataValue<>();
		valueList.setValue(LockLevel.RDU, linkList, rduLockLevelInfo);

		linkList.add(link1);
		linkList.add(link2);

		DataRow links = new DataRow(xrfInsLinks, valueList);

		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfIsin", DataLevel.XRF_INS),
				isinVal);

	
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfCusip", DataLevel.XRF_INS),
				cusip1);

		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfInstrumentStatus", DataLevel.XRF_INS), insStatus);

		container.addAttributeValue(xrfInsLinks, links);

		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);
		container.addDataContainer(secContainer, DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		sedol1.setValue(LockLevel.RDU, "sedol201", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "sedol202", rduLockLevelInfo);

		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);
		DataRow seclink2 = new DataRow(xrfSecLink);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId2);

		DataRow seclink1 = new DataRow(xrfSecLink);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId1);

		DataAttribute xrfsecLinks = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, rduLockLevelInfo);

		seclinkList.add(seclink1);
		seclinkList.add(seclink2);
		DataRow seclinks = new DataRow(xrfsecLinks, secvalueList);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSecurityStatus", DataLevel.XRF_SEC), insStatus);

		secContainer.addAttributeValue(xrfsecLinks, seclinks);

		return container;

	}
     
	public DataContainer getDataConatinerwithoutLinks1()
	{
		DataContainer container = DataContainerTestUtil.getDataContainer(DataLevel.XRF_INS);

		DataValue<String> instrumentId1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		instrumentId1.setValue(LockLevel.RDU, "I101", rduLockLevelInfo);

		DataValue<String> instrumentId2 = new DataValue<>();
		instrumentId2.setValue(LockLevel.RDU, "I102", rduLockLevelInfo);

		DataValue<String> isinVal = new DataValue<>();
		isinVal.setValue(LockLevel.RDU, "isin101", rduLockLevelInfo);

		DataValue<String> isinVal2 = new DataValue<>();
		isinVal2.setValue(LockLevel.RDU, "isin102", rduLockLevelInfo);

		DataValue<String> cusip1 = new DataValue<>();
		cusip1.setValue(LockLevel.RDU, "cusip101", rduLockLevelInfo);

		DataValue<String> cusip2 = new DataValue<>();
		cusip2.setValue(LockLevel.RDU, "cusip102", rduLockLevelInfo);

		DataValue<DomainType> assetType1 = new DataValue<>();
		DomainType as1 = new DomainType();
		as1.setNormalizedValue("asset101");
		assetType1.setValue(LockLevel.RDU, as1, rduLockLevelInfo);

		DataValue<DomainType> assetType2 = new DataValue<>();
		DomainType as2 = new DomainType();
		as2.setNormalizedValue("asset102");
		assetType2.setValue(LockLevel.RDU, as2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource1 = new DataValue<>();
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS101");
		instrumentXrfdataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource2 = new DataValue<>();
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS101");
		instrumentXrfdataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus1 = new DataValue<>();
		instrumentXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus2 = new DataValue<>();
		instrumentXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("A"), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate1 = new DataValue<>();
		instrumentLinkStartDate1.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate2 = new DataValue<>();
		instrumentLinkStartDate2.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);


		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I"), rduLockLevelInfo);

		DataValue<ReferenceId> instrumentRef1 = new DataValue<>();
		instrumentRef1.setValue(LockLevel.RDU, new ReferenceId("I101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> instrumentRef2 = new DataValue<>();
		instrumentRef2.setValue(LockLevel.RDU, new ReferenceId("I102", "D102"), rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC101");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC102");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC101");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC102");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC101", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC102", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS101", rduLockLevelInfo);

		DataValue<String> _instrument = new DataValue<>();
		_instrument.setValue(LockLevel.RDU, "XRI101", rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S102", "D102"), rduLockLevelInfo);



		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfIsin", DataLevel.XRF_INS),
				isinVal);


		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfCusip", DataLevel.XRF_INS),
				cusip1);
		


		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);
		container.addDataContainer(secContainer, DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		sedol1.setValue(LockLevel.RDU, "sedol101", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "sedol102", rduLockLevelInfo);


		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
		return container;

	
	}

	public DataContainer getChildDataContainer1() {
		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		sedol1.setValue(LockLevel.RDU, "sedol101", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "sedol102", rduLockLevelInfo);
		
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS101");

		
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS101");
		
		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU, new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I"), rduLockLevelInfo);
		
		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S102", "D102"), rduLockLevelInfo);
		
		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC101");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC102");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC101");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC102");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC101", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC102", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS101", rduLockLevelInfo);

		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);
		DataRow seclink2 = new DataRow(xrfSecLink);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId2);

		DataRow seclink1 = new DataRow(xrfSecLink);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId1);

		DataAttribute xrfsecLinks = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, rduLockLevelInfo);

		seclinkList.add(seclink1);
		seclinkList.add(seclink2);
		DataRow seclinks = new DataRow(xrfsecLinks, secvalueList);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
		
		secContainer.addAttributeValue(xrfsecLinks, seclinks);

		return secContainer;

	}
	
	public DataContainer getChildWithNoRefId() {

		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		sedol1.setValue(LockLevel.RDU, "sedol101", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "sedol102", rduLockLevelInfo);
		
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS101");

		
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS101");
		
		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I"), rduLockLevelInfo);
		
		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S102", "D102"), rduLockLevelInfo);
		
		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC101");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC102");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC101");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC102");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC101", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC102", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS101", rduLockLevelInfo);

		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);
		DataRow seclink2 = new DataRow(xrfSecLink);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId2);
	//	seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
		//		_securityXrfRefId2);

		DataRow seclink1 = new DataRow(xrfSecLink);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
	//	seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
		//		_securityXrfRefId1);

		DataAttribute xrfsecLinks = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, rduLockLevelInfo);

		seclinkList.add(seclink1);
		seclinkList.add(seclink2);
		DataRow seclinks = new DataRow(xrfsecLinks, secvalueList);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
	
		secContainer.addAttributeValue(xrfsecLinks, seclinks);

		return secContainer;

	
	}
	
	public DataContainer getChildDataContainerInput() {
		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		sedol1.setValue(LockLevel.RDU, "sedol103", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "sedol102", rduLockLevelInfo);
		
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS103");

		
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS101");
		
		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I"), rduLockLevelInfo);
		
		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S103", "D103"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S102", "D102"), rduLockLevelInfo);
		
		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC101");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC102");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC101");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC102");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC101", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC102", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS101", rduLockLevelInfo);

		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);
		DataRow seclink2 = new DataRow(xrfSecLink);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId2);

		DataRow seclink1 = new DataRow(xrfSecLink);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId1);

		DataAttribute xrfsecLinks = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, rduLockLevelInfo);

		seclinkList.add(seclink1);
		seclinkList.add(seclink2);
		DataRow seclinks = new DataRow(xrfsecLinks, secvalueList);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
	
		secContainer.addAttributeValue(xrfsecLinks, seclinks);

		return secContainer;

	}
	
	public DataContainer getSecContainerWithoutLinks() {
		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		sedol1.setValue(LockLevel.RDU, "sedol103", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "sedol102", rduLockLevelInfo);
		
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS103");

		
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS101");
		
		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I"), rduLockLevelInfo);
		
		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S103", "D103"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S102", "D102"), rduLockLevelInfo);
		
		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC101");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC102");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC101");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC102");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC101", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC102", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS101", rduLockLevelInfo);



		

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
	

		return secContainer;

	}

	public DataContainer getChildDataContainerUnmatched() {
		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		sedol1.setValue(LockLevel.RDU, "sedol103", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "sedol102", rduLockLevelInfo);
		
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS103");

		
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS101");
		
		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I"), rduLockLevelInfo);
		
		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S103", "D103"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S105", "D105"), rduLockLevelInfo);
		
		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC101");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC102");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC101");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC102");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC101", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC102", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS101", rduLockLevelInfo);

		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);
		DataRow seclink2 = new DataRow(xrfSecLink);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource2);
		seclink2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId2);
		seclink2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId2);

		DataRow seclink1 = new DataRow(xrfSecLink);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("sedol", DataLevel.XRF_SEC), sedol1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkDataSource", DataLevel.XRF_SEC),
				securityXrfLinkDataSource1);
		seclink1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinkStatus", DataLevel.XRF_SEC),
				securityXrfLinkStatus1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel(TRADE_CRR, DataLevel.XRF_SEC),
				tradeCurrency1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("exchangeCode", DataLevel.XRF_SEC),
				exchangeCode1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("rduSecurityId", DataLevel.XRF_SEC),
				rduSecurtyId1);
		seclink1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_securityXrfRefId", DataLevel.XRF_SEC),
				_securityXrfRefId1);

		DataAttribute xrfsecLinks = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);

		ArrayList<DataRow> seclinkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, rduLockLevelInfo);

		seclinkList.add(seclink1);
		seclinkList.add(seclink2);
		DataRow seclinks = new DataRow(xrfsecLinks, secvalueList);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
		
		secContainer.addAttributeValue(xrfsecLinks, seclinks);

		return secContainer;

	}
	
	public DataContainer getContainerWithoutSecurity() {


		DataContainer container = DataContainerTestUtil.getDataContainer(DataLevel.XRF_INS);

		DataValue<String> instrumentId1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		instrumentId1.setValue(LockLevel.RDU, "I201", rduLockLevelInfo);
		
		DataValue<DomainType> insStatus = new DataValue<>();
		DomainType status= new DomainType();
		status.setNormalizedValue("A");
		insStatus.setValue(LockLevel.RDU,status, rduLockLevelInfo);

		DataValue<String> instrumentId2 = new DataValue<>();
		instrumentId2.setValue(LockLevel.RDU, "I202", rduLockLevelInfo);

		DataValue<String> isinVal = new DataValue<>();
		isinVal.setValue(LockLevel.RDU, "isin201", rduLockLevelInfo);

		DataValue<String> isinVal2 = new DataValue<>();
		isinVal2.setValue(LockLevel.RDU, "isin202", rduLockLevelInfo);

		DataValue<String> cusip1 = new DataValue<>();
		cusip1.setValue(LockLevel.RDU, "cusip201", rduLockLevelInfo);

		DataValue<String> cusip2 = new DataValue<>();
		cusip2.setValue(LockLevel.RDU, "cusip202", rduLockLevelInfo);

		DataValue<DomainType> assetType1 = new DataValue<>();
		DomainType as1 = new DomainType();
		as1.setNormalizedValue("asset201");
		assetType1.setValue(LockLevel.RDU, as1, rduLockLevelInfo);

		DataValue<DomainType> assetType2 = new DataValue<>();
		DomainType as2 = new DomainType();
		as2.setNormalizedValue("asset202");
		assetType2.setValue(LockLevel.RDU, as2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource1 = new DataValue<>();
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS201");
		instrumentXrfdataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource2 = new DataValue<>();
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS201");
		instrumentXrfdataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus1 = new DataValue<>();
		instrumentXrfLinkStatus1.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus2 = new DataValue<>();
		instrumentXrfLinkStatus2.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate1 = new DataValue<>();
		instrumentLinkStartDate1.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate2 = new DataValue<>();
		instrumentLinkStartDate2.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I"), rduLockLevelInfo);

		DataValue<ReferenceId> instrumentRef1 = new DataValue<>();
		instrumentRef1.setValue(LockLevel.RDU, new ReferenceId("I101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> instrumentRef2 = new DataValue<>();
		instrumentRef2.setValue(LockLevel.RDU, new ReferenceId("I202", "D202"), rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC201");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC202");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC201");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC202");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC201", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC202", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS201", rduLockLevelInfo);

		DataValue<String> _instrument = new DataValue<>();
		_instrument.setValue(LockLevel.RDU, "XRI201", rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S202", "D202"), rduLockLevelInfo);

		DataAttribute xrfInsLink = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);
		DataRow link2 = new DataRow(xrfInsLink);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("cusip", DataLevel.XRF_INS), cusip2);

		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel(INSTRUMENT_XRFDATA_SOURCE, DataLevel.XRF_INS),
				instrumentXrfdataSource2);
		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS),
				instrumentXrfLinkStatus2);
		link2.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS),
				instrumentRef2);
		link2.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkInitialDate", DataLevel.XRF_INS),
				instrumentLinkStartDate2);

		DataRow link1 = new DataRow(xrfInsLink);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS), isinVal2);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel(INSTRUMENT_XRFDATA_SOURCE, DataLevel.XRF_INS),
				instrumentXrfdataSource1);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkStatus", DataLevel.XRF_INS),
				instrumentXrfLinkStatus1);
		link1.addAttribute(DataAttributeFactory.getAttributeByNameAndLevel("_instrumentXrfRefId", DataLevel.XRF_INS),
				instrumentRef1);
		link1.addAttribute(
				DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinkInitialDate", DataLevel.XRF_INS),
				instrumentLinkStartDate1);

		DataAttribute xrfInsLinks = DataAttributeFactory.getAttributeByNameAndLevel("instrumentXrfLinks",
				DataLevel.XRF_INS);

		ArrayList<DataRow> linkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> valueList = new DataValue<>();
		valueList.setValue(LockLevel.RDU, linkList, rduLockLevelInfo);

		linkList.add(link1);
		linkList.add(link2);

		DataRow links = new DataRow(xrfInsLinks, valueList);

		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfIsin", DataLevel.XRF_INS),
				isinVal);

		
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfCusip", DataLevel.XRF_INS),
				cusip1);
		
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfInstrumentStatus", DataLevel.XRF_INS), insStatus);

		container.addAttributeValue(xrfInsLinks, links);

		return container;

	
	}
	
	public DataContainer getDataContainerwithoutLinks2() {

		DataContainer container = DataContainerTestUtil.getDataContainer(DataLevel.XRF_INS);

		DataValue<String> instrumentId1 = new DataValue<>();
		RduLockLevelInfo rduLockLevelInfo = new RduLockLevelInfo();
		instrumentId1.setValue(LockLevel.RDU, "I201", rduLockLevelInfo);

		DataValue<String> instrumentId2 = new DataValue<>();
		instrumentId2.setValue(LockLevel.RDU, "I202", rduLockLevelInfo);

		DataValue<String> isinVal = new DataValue<>();
		isinVal.setValue(LockLevel.RDU, "isin201", rduLockLevelInfo);

		DataValue<String> isinVal2 = new DataValue<>();
		isinVal2.setValue(LockLevel.RDU, "isin202", rduLockLevelInfo);

		DataValue<String> cusip1 = new DataValue<>();
		cusip1.setValue(LockLevel.RDU, "cusip201", rduLockLevelInfo);

		DataValue<String> cusip2 = new DataValue<>();
		cusip2.setValue(LockLevel.RDU, "cusip202", rduLockLevelInfo);

		DataValue<DomainType> assetType1 = new DataValue<>();
		DomainType as1 = new DomainType();
		as1.setNormalizedValue("asset201");
		assetType1.setValue(LockLevel.RDU, as1, rduLockLevelInfo);

		DataValue<DomainType> assetType2 = new DataValue<>();
		DomainType as2 = new DomainType();
		as2.setNormalizedValue("asset202");
		assetType2.setValue(LockLevel.RDU, as2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource1 = new DataValue<>();
		DomainType dataSource1 = new DomainType();
		dataSource1.setNormalizedValue("DS201");
		instrumentXrfdataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfdataSource2 = new DataValue<>();
		DomainType dataSource2 = new DomainType();
		dataSource1.setNormalizedValue("DS201");
		instrumentXrfdataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus1 = new DataValue<>();
		instrumentXrfLinkStatus1.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> instrumentXrfLinkStatus2 = new DataValue<>();
		instrumentXrfLinkStatus2.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate1 = new DataValue<>();
		instrumentLinkStartDate1.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);

		DataValue<LocalDateTime> instrumentLinkStartDate2 = new DataValue<>();
		instrumentLinkStartDate2.setValue(LockLevel.RDU, LocalDateTime.now(), rduLockLevelInfo);


		DataValue<DomainType> securityXrfLinkDataSource1 = new DataValue<>();
		securityXrfLinkDataSource1.setValue(LockLevel.RDU, dataSource1, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkDataSource2 = new DataValue<>();
		securityXrfLinkDataSource2.setValue(LockLevel.RDU, dataSource2, rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus1 = new DataValue<>();
		securityXrfLinkStatus1.setValue(LockLevel.RDU,new DomainType("A"), rduLockLevelInfo);

		DataValue<DomainType> securityXrfLinkStatus2 = new DataValue<>();
		securityXrfLinkStatus2.setValue(LockLevel.RDU, new DomainType("I"), rduLockLevelInfo);


		DataValue<ReferenceId> instrumentRef1 = new DataValue<>();
		instrumentRef1.setValue(LockLevel.RDU, new ReferenceId("I101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> instrumentRef2 = new DataValue<>();
		instrumentRef2.setValue(LockLevel.RDU, new ReferenceId("I202", "D202"), rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency1 = new DataValue<>();
		DomainType tc1 = new DomainType();
		tc1.setNormalizedValue("TC201");
		tradeCurrency1.setValue(LockLevel.RDU, tc1, rduLockLevelInfo);

		DataValue<DomainType> tradeCurrency2 = new DataValue<>();
		DomainType tc2 = new DomainType();
		tc2.setNormalizedValue("TC202");
		tradeCurrency2.setValue(LockLevel.RDU, tc2, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode1 = new DataValue<>();
		DomainType ec1 = new DomainType();
		ec1.setNormalizedValue("EC201");
		exchangeCode1.setValue(LockLevel.RDU, ec1, rduLockLevelInfo);

		DataValue<DomainType> exchangeCode2 = new DataValue<>();
		DomainType ec2 = new DomainType();
		ec2.setNormalizedValue("EC202");
		exchangeCode2.setValue(LockLevel.RDU, ec2, rduLockLevelInfo);

		DataValue<String> rduSecurtyId1 = new DataValue<>();
		rduSecurtyId1.setValue(LockLevel.RDU, "RDUSEC201", rduLockLevelInfo);

		DataValue<String> rduSecurtyId2 = new DataValue<>();
		rduSecurtyId2.setValue(LockLevel.RDU, "RDUSEC202", rduLockLevelInfo);

		DataValue<String> _securityId = new DataValue<>();
		_securityId.setValue(LockLevel.RDU, "XRS201", rduLockLevelInfo);

		DataValue<String> _instrument = new DataValue<>();
		_instrument.setValue(LockLevel.RDU, "XRI201", rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId1 = new DataValue<>();
		_securityXrfRefId1.setValue(LockLevel.RDU, new ReferenceId("S101", "D101"), rduLockLevelInfo);

		DataValue<ReferenceId> _securityXrfRefId2 = new DataValue<>();
		_securityXrfRefId2.setValue(LockLevel.RDU, new ReferenceId("S202", "D202"), rduLockLevelInfo);


		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfIsin", DataLevel.XRF_INS),
				isinVal);

		
		container.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfCusip", DataLevel.XRF_INS),
				cusip1);
	


		DataContainer secContainer = DataContainerTestUtil.getDataContainer(DataLevel.XRF_SEC);
		container.addDataContainer(secContainer, DataLevel.XRF_SEC);

		DataValue<String> sedol1 = new DataValue<>();
		sedol1.setValue(LockLevel.RDU, "sedol201", rduLockLevelInfo);

		DataValue<String> sedol2 = new DataValue<>();
		sedol2.setValue(LockLevel.RDU, "sedol202", rduLockLevelInfo);

		secContainer.addAttributeValue(DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC),
				sedol1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC), exchangeCode1);
		secContainer.addAttributeValue(
				DataAttributeFactory.getAttributeByNameAndLevel(XRF_TRADE_CURR, DataLevel.XRF_SEC), tradeCurrency1);
	

		return container;

	
	}
}

