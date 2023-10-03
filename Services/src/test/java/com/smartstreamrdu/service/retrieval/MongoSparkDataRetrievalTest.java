package com.smartstreamrdu.service.retrieval;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataContainerContext;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataValue;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.LockLevelInfo;
import com.smartstreamrdu.domain.ProformaCustomAttributeType;
import com.smartstreamrdu.domain.RduLockLevelInfo;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.persistence.retrival.Criteria;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.service.PersistenceEntityRepository;
import com.smartstreamrdu.persistence.service.PersistenceServiceImpl;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.spark.SparkUtil;
import com.smartstreamrdu.util.Constant;
import com.smartstreamrdu.util.ProformaConstants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class MongoSparkDataRetrievalTest{
	
	@Autowired
	private MongoSparkDataRetrieval sparkDataRetrieval;
	
	@Autowired
	private PersistenceServiceImpl persistenceService;

	@Autowired
	private MongoTemplate mongo;
	
	@Autowired
	private PersistenceEntityRepository repo;
	
	@Value("${mongoDBUri}")
	private String mongoDbUri;
	
	protected SparkSession session ;
	
	private static final DataAttribute xrfIsinAttr = DataAttributeFactory.getAttributeByNameAndLevel("xrfIsin", DataLevel.XRF_INS);
	private static final DataAttribute xrfSedolAttr=DataAttributeFactory.getAttributeByNameAndLevel("xrfSedol", DataLevel.XRF_SEC);
	private static final DataAttribute xrfExchangeCodeAttr=DataAttributeFactory.getAttributeByNameAndLevel("xrfExchangeCode", DataLevel.XRF_SEC);
	private static final DataAttribute xrfSecLinkStatusAttr=DataAttributeFactory.getAttributeByNameAndLevel(Constant.CrossRefConstants.XR_SEC_LINK_STATUS, DataLevel.XRF_SEC);
	private static final DataAttribute xrfInsLinkIsinAttr=DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.XRF_INS);
	private static final DataAttribute xrfInsLinkCusipAttr=DataAttributeFactory.getAttributeByNameAndLevel("cusip", DataLevel.XRF_INS);
	private static final DataAttribute xrfSecLinkDatSourceAttr=DataAttributeFactory.getAttributeByNameAndLevel(Constant.CrossRefConstants.SEC_DATASOURCE, DataLevel.XRF_SEC);
	
	@Before
	public void setUp(){
		initializeSpark();
		createXrData();
		try {
			createProformaData();
		} catch (UdmTechnicalException e) {
			e.printStackTrace();
		}
	}
	
	public  void createProformaData() throws UdmTechnicalException{
		mongo.remove(new Query(), ProformaConstants.PROFORMA_COLLECTION_PREFIX+"ESMA");
		
		DataContainer container = new DataContainer(DataLevel.PROFORMA_INS, null);
		LockLevelInfo noneLockinfo = new LockLevelInfo(LockLevel.NONE);

		DataAttribute isin = DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.PROFORMA_INS);
		DataAttribute instrumentTypeCode = DataAttributeFactory.getAttributeByNameAndLevel("instrumentTypeCode",
				DataLevel.PROFORMA_INS);
		DataAttribute cfiCode2015 = DataAttributeFactory.getAttributeByNameAndLevel("cfiCode2015",
				DataLevel.PROFORMA_INS);
		DataAttribute maturityDate = DataAttributeFactory.getAttributeByNameAndLevel("maturityDate",
				DataLevel.PROFORMA_INS);
		DataAttribute currentNominalCurrencyCode = DataAttributeFactory
				.getAttributeByNameAndLevel("currentNominalCurrencyCode", DataLevel.PROFORMA_INS);

		// isin
		DataRow row1 = new DataRow(isin);
		DataAttribute source = DataAttributeFactory.getAttributeByNameAndLevelAndParent("source",
				DataLevel.PROFORMA_INS, isin);
		DataAttribute value = DataAttributeFactory.getAttributeByNameAndLevelAndParent("value", DataLevel.PROFORMA_INS,
				isin);

		DataValue<String> sourceVal = new DataValue<>();
		sourceVal.setValue(LockLevel.NONE, "esmaFirds", noneLockinfo);
		row1.addAttribute(source, sourceVal);

		DataValue<String> val = new DataValue<>();
		val.setValue(LockLevel.NONE, "LU0048587603", noneLockinfo);
		row1.addAttribute(value, val);

		DataValue<ArrayList<DataRow>> disProFormaValue = new DataValue<>();
		ArrayList<DataRow> rowValueList = new ArrayList<>();
		rowValueList.add(row1);
		disProFormaValue.setValue(LockLevel.NONE, rowValueList, noneLockinfo);

		DataRow dr = new DataRow(isin, disProFormaValue);
		container.addAttributeValue(isin, dr);

		// instrumentTypeCode
		DataAttribute insTypeCodeSource = DataAttributeFactory.getAttributeByNameAndLevelAndParent("source",
				DataLevel.PROFORMA_INS, instrumentTypeCode);
		DataAttribute insTypeCodeValue = DataAttributeFactory.getAttributeByNameAndLevelAndParent("value",
				DataLevel.PROFORMA_INS, instrumentTypeCode);
		DataAttribute insTypeCodeDomain = DataAttributeFactory.getAttributeByNameAndLevelAndParent("domain",
				DataLevel.PROFORMA_INS, instrumentTypeCode);

		DataRow row2 = new DataRow(instrumentTypeCode);

		DataValue<String> sourceVal2 = new DataValue<>();
		sourceVal2.setValue(LockLevel.NONE, "esmaFirds", noneLockinfo);
		row2.addAttribute(insTypeCodeSource, sourceVal2);

		DataValue<String> val2 = new DataValue<>();
		val2.setValue(LockLevel.NONE, "OPF", noneLockinfo);
		row2.addAttribute(insTypeCodeValue, val2);

		DataValue<String> domainVal = new DataValue<>();
		domainVal.setValue(LockLevel.NONE, "RDU", noneLockinfo);
		row2.addAttribute(insTypeCodeDomain, domainVal);

		DataValue<ArrayList<DataRow>> disProFormaValue2 = new DataValue<>();
		ArrayList<DataRow> rowValueList2 = new ArrayList<>();
		rowValueList2.add(row2);
		disProFormaValue2.setValue(LockLevel.NONE, rowValueList2, noneLockinfo);

		DataRow dr2 = new DataRow(instrumentTypeCode, disProFormaValue2);
		container.addAttributeValue(instrumentTypeCode, dr2);

		// cfiCode2015
		DataAttribute cfiCode2015Source = DataAttributeFactory.getAttributeByNameAndLevelAndParent("source",
				DataLevel.PROFORMA_INS, cfiCode2015);
		DataAttribute cfiCode2015Value = DataAttributeFactory.getAttributeByNameAndLevelAndParent("value",
				DataLevel.PROFORMA_INS, cfiCode2015);

		DataRow row3 = new DataRow(cfiCode2015);

		DataValue<String> cfiCode2015SourceVal = new DataValue<>();
		cfiCode2015SourceVal.setValue(LockLevel.NONE, "esmaFirds", noneLockinfo);
		row3.addAttribute(cfiCode2015Source, cfiCode2015SourceVal);

		DataValue<String> val3 = new DataValue<>();
		val3.setValue(LockLevel.NONE, "CIOGES", noneLockinfo);
		row3.addAttribute(cfiCode2015Value, val3);

		DataValue<ArrayList<DataRow>> disProFormaValue3 = new DataValue<>();
		ArrayList<DataRow> rowValueList3 = new ArrayList<>();
		rowValueList3.add(row3);
		disProFormaValue3.setValue(LockLevel.NONE, rowValueList3, noneLockinfo);

		DataRow dr3 = new DataRow(cfiCode2015, disProFormaValue3);
		container.addAttributeValue(cfiCode2015, dr3);

		// maturityDate
		DataAttribute maturityDateSource = DataAttributeFactory.getAttributeByNameAndLevelAndParent("source",
				DataLevel.PROFORMA_INS, maturityDate);
		DataAttribute maturityDateErrorCode = DataAttributeFactory.getAttributeByNameAndLevelAndParent("errorCode",
				DataLevel.PROFORMA_INS, maturityDate);

		DataRow row4 = new DataRow(maturityDate);

		DataValue<String> maturityDateSourceVal = new DataValue<>();
		maturityDateSourceVal.setValue(LockLevel.NONE, "esmaFirds", noneLockinfo);
		row4.addAttribute(maturityDateSource, maturityDateSourceVal);

		DataValue<Integer> errorCode = new DataValue<>();
		errorCode.setValue(LockLevel.NONE, 101, noneLockinfo);
		row4.addAttribute(maturityDateErrorCode, errorCode);

		DataValue<ArrayList<DataRow>> disProFormaValue4 = new DataValue<>();
		ArrayList<DataRow> rowValueList4 = new ArrayList<>();
		rowValueList4.add(row4);
		disProFormaValue4.setValue(LockLevel.NONE, rowValueList4, noneLockinfo);

		DataRow dr4 = new DataRow(maturityDate, disProFormaValue4);
		container.addAttributeValue(maturityDate, dr4);

		// currentNominalCurrencyCode
		DataAttribute currentNominalCurrencyCodeSource = DataAttributeFactory
				.getAttributeByNameAndLevelAndParent("source", DataLevel.PROFORMA_INS, currentNominalCurrencyCode);
		DataAttribute currentNominalCurrencyCodeValue = DataAttributeFactory
				.getAttributeByNameAndLevelAndParent("value", DataLevel.PROFORMA_INS, currentNominalCurrencyCode);
		DataAttribute currentNominalCurrencyCodeDomain = DataAttributeFactory
				.getAttributeByNameAndLevelAndParent("domain", DataLevel.PROFORMA_INS, currentNominalCurrencyCode);

		DataRow row5 = new DataRow(currentNominalCurrencyCode);

		DataValue<String> sourceVal5 = new DataValue<>();
		sourceVal5.setValue(LockLevel.NONE, "esmaFirds", noneLockinfo);
		row5.addAttribute(currentNominalCurrencyCodeSource, sourceVal5);

		DataValue<String> val5 = new DataValue<>();
		val5.setValue(LockLevel.NONE, "currentNominalCurrencyCode1", noneLockinfo);
		row5.addAttribute(currentNominalCurrencyCodeValue, val5);

		DataValue<String> domainVal2 = new DataValue<>();
		domainVal2.setValue(LockLevel.NONE, "RDU", noneLockinfo);
		row5.addAttribute(currentNominalCurrencyCodeDomain, domainVal2);

		DataValue<ArrayList<DataRow>> disProFormaValue5 = new DataValue<>();
		ArrayList<DataRow> rowValueList5 = new ArrayList<>();
		rowValueList5.add(row5);
		disProFormaValue5.setValue(LockLevel.NONE, rowValueList5, noneLockinfo);

		DataRow dr5 = new DataRow(currentNominalCurrencyCode, disProFormaValue5);
		container.addAttributeValue(currentNominalCurrencyCode, dr5);

		// disCustomAttributes
		DataAttribute clientAttributes = DataAttributeFactory.getAttributeByNameAndLevel("disCustomAttributes",
				DataLevel.PROFORMA_INS);
		DataValue<ProformaCustomAttributeType> clientAttrMap = new DataValue<>();

		ProformaCustomAttributeType disOnlyType = new ProformaCustomAttributeType();

		LinkedHashMap<String, Serializable> valuesMap = new LinkedHashMap<>();
		valuesMap.put("source", "S&P");
		valuesMap.put("value", "A");

		LinkedHashMap<String, Serializable> valuesMap11 = new LinkedHashMap<>();
		valuesMap11.put("source", "MDY");
		valuesMap11.put("value", "A1");

		LinkedHashMap<String, Serializable> valuesMap1 = new LinkedHashMap<>();
		valuesMap1.put("source", "esmaFirds");
		valuesMap1.put("value", "GOVS");

		ArrayList<HashMap<String, Serializable>> list1 = new ArrayList<>();
		ArrayList<HashMap<String, Serializable>> list2 = new ArrayList<>();

		list1.add(valuesMap);
		list1.add(valuesMap11);

		list2.add(valuesMap1);

		disOnlyType.addAttributeAndValue("securityQuality", list1);
		disOnlyType.addAttributeAndValue("securityType", list2);

		clientAttrMap.setValue(LockLevel.NONE, disOnlyType, noneLockinfo);

		container.addAttributeValue(clientAttributes, clientAttrMap);

		// instrumentLegalEntityRelations

		DataAttribute insLeRelationAttribute = DataAttributeFactory
				.getAttributeByNameAndLevel("instrumentLegalEntityRelations", DataLevel.PROFORMA_INS);
		DataAttribute refData = DataAttributeFactory.getAttributeByNameAndLevelAndParent("refData",
				DataLevel.PROFORMA_INS, insLeRelationAttribute);
		DataRow refDataRow = new DataRow(refData);

		DataAttribute relationType = DataAttributeFactory.getAttributeByNameAndLevelAndParent("relationType",
				DataLevel.PROFORMA_INS,insLeRelationAttribute);

		DataRow insLeRow = new DataRow(insLeRelationAttribute);

		DataValue<String> relationVal = new DataValue<>();
		relationVal.setValue(LockLevel.NONE, "issuers", noneLockinfo);
		insLeRow.addAttribute(relationType, relationVal);

		DataAttribute lei = DataAttributeFactory.getAttributeByNameAndLevel("lei", DataLevel.PROFORMA_LE);

		DataAttribute leValue = DataAttributeFactory.getAttributeByNameAndLevelAndParent("value",
				DataLevel.PROFORMA_LE, lei);
		DataAttribute leSource = DataAttributeFactory.getAttributeByNameAndLevelAndParent("source",
				DataLevel.PROFORMA_LE, lei);

		DataRow leiRelationRow = new DataRow(lei);

		DataValue<String> leValue1 = new DataValue<>();
		leValue1.setValue(LockLevel.NONE, "LU0048587603", noneLockinfo);
		leiRelationRow.addAttribute(leValue, leValue1);

		DataValue<String> leSourceVal = new DataValue<>();
		leSourceVal.setValue(LockLevel.NONE, "GLIEF", noneLockinfo);
		leiRelationRow.addAttribute(leSource, leSourceVal);

		ArrayList<DataRow> leList = new ArrayList<>();
		leList.add(leiRelationRow);
		DataValue<ArrayList<DataRow>> LeiRelationTypeList = new DataValue<>();
		LeiRelationTypeList.setValue(LockLevel.NONE, leList, noneLockinfo);

		DataRow leiRelationLinks = new DataRow(lei, LeiRelationTypeList);
		refDataRow.addAttribute(lei, leiRelationLinks);

		insLeRow.addAttribute(refData, refDataRow);

		ArrayList<DataRow> insLeRelationsList = new ArrayList<>();
		insLeRelationsList.add(insLeRow);

		DataValue<ArrayList<DataRow>> insLeRelationsListVal = new DataValue<>();
		insLeRelationsListVal.setValue(LockLevel.NONE, insLeRelationsList, noneLockinfo);

		DataRow links1 = new DataRow(insLeRelationAttribute, insLeRelationsListVal);

		container.addAttributeValue(insLeRelationAttribute, links1);
		
		//instrumentStatus value
        DataAttribute instrumentStatus = DataAttributeFactory
                .getAttributeByNameAndLevel("instrumentStatus", DataLevel.PROFORMA_INS);
        DataAttribute instrumentStatusCodeSource = DataAttributeFactory
                .getAttributeByNameAndLevelAndParent("source", DataLevel.PROFORMA_INS, instrumentStatus);
        DataAttribute instrumentStatusValue = DataAttributeFactory
                .getAttributeByNameAndLevelAndParent("value", DataLevel.PROFORMA_INS, instrumentStatus);
        DataAttribute instrumentStatusDomain = DataAttributeFactory
                .getAttributeByNameAndLevelAndParent("domain", DataLevel.PROFORMA_INS, instrumentStatus);


        DataRow row8 = new DataRow(instrumentStatus);


        DataValue<String> sourceVal8 = new DataValue<>();
        sourceVal8.setValue(LockLevel.NONE, "esmaFirds", noneLockinfo);
        row8.addAttribute(instrumentStatusCodeSource, sourceVal8);


        DataValue<String> val8 = new DataValue<>();
        val8.setValue(LockLevel.NONE, "A", noneLockinfo);
        row8.addAttribute(instrumentStatusValue, val8);


        DataValue<String> domainVal8 = new DataValue<>();
        domainVal8.setValue(LockLevel.NONE, "RDU", noneLockinfo);
        row8.addAttribute(instrumentStatusDomain, domainVal8);


        DataValue<ArrayList<DataRow>> disProFormaValue8 = new DataValue<>();
        ArrayList<DataRow> rowValueList8 = new ArrayList<>();
        rowValueList8.add(row8);
        disProFormaValue8.setValue(LockLevel.NONE, rowValueList8, noneLockinfo);


        DataRow dr8 = new DataRow(instrumentStatus, disProFormaValue8);
        container.addAttributeValue(instrumentStatus, dr8);


        DataAttribute latModifiedDate = DataAttributeFactory.getAttributeByNameAndLevel("lastModifiedDate", DataLevel.PROFORMA_DOC);
        LockLevelInfo levelinfo = new LockLevelInfo(LockLevel.NONE);
		DataValue<LocalDateTime> latModifiedDateVal = new DataValue<>();
		latModifiedDateVal.setValue(LockLevel.NONE, LocalDateTime.now(),levelinfo);
		
		container.addAttributeValue(latModifiedDate, latModifiedDateVal);
		persistenceService.persist(container, ProformaConstants.PROFORMA_COLLECTION_PREFIX+"ESMA");
	}
	
	private void createXrData() {
		mongo.remove(new Query(), repo.getRootClassForLevel(DataLevel.XRF_INS));
		
		
		DataContainer xrContainer=new  DataContainer(DataLevel.XRF_INS, DataContainerContext.builder().build());
		DataValue<String> xrfIsinVal=new DataValue<>();
		String xrfIsin="BMG2157R1189";
		xrfIsinVal.setValue(LockLevel.RDU, xrfIsin);
		xrContainer.addAttributeValue(xrfIsinAttr, xrfIsinVal);
		
		DataContainer xrSecContainer=new DataContainer(DataLevel.XRF_SEC, DataContainerContext.builder().build());
		xrContainer.addDataContainer(xrSecContainer, DataLevel.XRF_SEC);
		DataValue<DomainType> xrfExchangeCodeVal=new DataValue<>();
		String xrfExchangeCode="XNYS";
		xrfExchangeCodeVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfExchangeCode));
		xrSecContainer.addAttributeValue(xrfExchangeCodeAttr, xrfExchangeCodeVal);
		
		DataValue<String> xrfSedolVal=new DataValue<>();
		String xrfSedol="sedol";
		xrfSedolVal.setValue(LockLevel.RDU, xrfSedol);
		xrSecContainer.addAttributeValue(xrfSedolAttr, xrfSedolVal);
		
		DataAttribute xrfSecLink = DataAttributeFactory.getAttributeByNameAndLevel("securityXrfLinks",
				DataLevel.XRF_SEC);
		
		DataRow seclink1 = new DataRow(xrfSecLink);
		DataValue<DomainType> xrfSecLinkStatusVal=new DataValue<>();
		String xrfSecLinkStatusActive="A";
		xrfSecLinkStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfSecLinkStatusActive));
		seclink1.addAttribute(xrfSecLinkStatusAttr, xrfSecLinkStatusVal);
		
		DataValue<DomainType> dataSourceValue=new DataValue<>();
		String dataSourceTrdse="trdse";
		dataSourceValue.setValue(LockLevel.RDU, new DomainType(null, null, dataSourceTrdse));
		seclink1.addAttribute(xrfSecLinkDatSourceAttr, dataSourceValue);
		
		
		DataRow seclink2 = new DataRow(xrfSecLink);
		DataValue<DomainType> xrfSecLinkStatusVal2=new DataValue<>();
		String xrfSecLinkStatusInactive="I";
		xrfSecLinkStatusVal2.setValue(LockLevel.RDU, new DomainType(null, null, xrfSecLinkStatusInactive));
		seclink2.addAttribute(xrfSecLinkStatusAttr, xrfSecLinkStatusVal2);
		
		DataValue<DomainType> dataSourceValue2=new DataValue<>();
		String dataSourceFigi="figi";
		dataSourceValue2.setValue(LockLevel.RDU, new DomainType(null, null, dataSourceFigi));
		seclink2.addAttribute(xrfSecLinkDatSourceAttr, dataSourceValue2);
		

		ArrayList<DataRow> seclinkList = new ArrayList<>();

		DataValue<ArrayList<DataRow>> secvalueList = new DataValue<>();
		secvalueList.setValue(LockLevel.RDU, seclinkList, new RduLockLevelInfo());

		seclinkList.add(seclink1);
		seclinkList.add(seclink2);
		DataRow seclinks = new DataRow(xrfSecLink, secvalueList);
		
		xrSecContainer.addAttributeValue(xrfSecLink, seclinks);
		
		DataContainer xrSecContainer1=new DataContainer(DataLevel.XRF_SEC, DataContainerContext.builder().build());
		xrContainer.addDataContainer(xrSecContainer1, DataLevel.XRF_SEC);
		DataValue<DomainType> xrfExchangeCodeVal1=new DataValue<>();
		String xrfExchangeCode1="XLON";
		xrfExchangeCodeVal1.setValue(LockLevel.RDU, new DomainType(null, null, xrfExchangeCode1));
		xrSecContainer1.addAttributeValue(xrfExchangeCodeAttr, xrfExchangeCodeVal1);
		
		DataValue<String> xrfSedolVal1=new DataValue<>();
		String xrfSedol1="sedol";
		xrfSedolVal1.setValue(LockLevel.RDU, xrfSedol1);
		xrSecContainer1.addAttributeValue(xrfSedolAttr, xrfSedolVal1);
		
		persistenceService.persist(xrContainer);
	}
	public void initializeSpark(){
		SparkUtil u = SpringUtil.getBean(SparkUtil.class);
		//String srcPath = System.getProperty("user.dir");
		//System.getProperties().setProperty("hadoop.home.dir", srcPath);
		System.setProperty("spark.master", "local[4]");
		System.setProperty("spark.mongodb.input.uri", mongoDbUri + ".sdData");
		session = u.getSparkContext();
	}
	
	@Test
	 public void test_retrieveSparkRDD_recordFound() throws Exception{
		 DataRetrivalInput input=new DataRetrivalInput();
		 DataValue<String> xrfIsinVal=new DataValue<>();
			String xrfIsin="BMG2157R1189";
			xrfIsinVal.setValue(LockLevel.RDU, xrfIsin);
			Criteria c1=Criteria.where(xrfIsinAttr).is(xrfIsinVal);
			
			DataValue<String> xrfSedolVal=new DataValue<>();
			String xrfSedol="sedol";
			xrfSedolVal.setValue(LockLevel.RDU, xrfSedol);
			Criteria c2 = Criteria.where(xrfSedolAttr).is(xrfSedolVal);
			
			DataValue<DomainType> xrfExchangeCodeVal=new DataValue<>();
			String xrfExchangeCode="XNYS";
			xrfExchangeCodeVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfExchangeCode));
			Criteria c3=Criteria.where(xrfExchangeCodeAttr).is(xrfExchangeCodeVal);
			
			DataValue<DomainType> xrfSecLinkStatusVal=new DataValue<>();
			String xrfSecLinkStatus="A";
			xrfSecLinkStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfSecLinkStatus));
			Criteria c4=Criteria.where(xrfSecLinkStatusAttr).is(xrfSecLinkStatusVal);
			
			
			DataValue<DomainType> dataSourceValue=new DataValue<>();
			String dataSource="trdse";
			dataSourceValue.setValue(LockLevel.RDU, new DomainType(null, null, dataSource));
			Criteria c5 = Criteria.where(xrfSecLinkDatSourceAttr).is(dataSourceValue);
			
		 Criteria c=c1.andOperator(c2,c3,c4,c5);
		 input.setCriteria(c);
		 input.setLevel(DataLevel.XRF_INS);
		JavaRDD<DataContainer> rdd = sparkDataRetrieval.retrieveSparkRDD(input, session);
		Assert.assertEquals(1, rdd.count());
		DataContainer firstContaiuner = rdd.first();
		Assert.assertEquals(2, firstContaiuner.getAllChildDataContainers().size());
	 }
	
	@Test
	 public void test_retrieveSparkRDD_recordNotFound() throws Exception{

		 DataRetrivalInput input=new DataRetrivalInput();
		 DataValue<String> xrfIsinVal=new DataValue<>();
			String xrfIsin="BMG2157R1189";
			xrfIsinVal.setValue(LockLevel.RDU, xrfIsin);
			Criteria c1=Criteria.where(xrfIsinAttr).is(xrfIsinVal);
			
			DataValue<String> xrfSedolVal=new DataValue<>();
			String xrfSedol="sedol";
			xrfSedolVal.setValue(LockLevel.RDU, xrfSedol);
			Criteria c2 = Criteria.where(xrfSedolAttr).is(xrfSedolVal);
			
			DataValue<DomainType> xrfExchangeCodeVal=new DataValue<>();
			String xrfExchangeCode="XNYS";
			xrfExchangeCodeVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfExchangeCode));
			Criteria c3=Criteria.where(xrfExchangeCodeAttr).is(xrfExchangeCodeVal);
			
			DataValue<DomainType> xrfSecLinkStatusVal=new DataValue<>();
			String xrfSecLinkStatus="A";
			xrfSecLinkStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfSecLinkStatus));
			Criteria c4=Criteria.where(xrfSecLinkStatusAttr).is(xrfSecLinkStatusVal);
			
			
			DataValue<DomainType> dataSourceValue=new DataValue<>();
			String dataSource="figi";
			dataSourceValue.setValue(LockLevel.RDU, new DomainType(null, null, dataSource));
			Criteria c5 = Criteria.where(xrfSecLinkDatSourceAttr).is(dataSourceValue);
			
		 Criteria c=c1.andOperator(c2,c3,c4,c5);
		 input.setCriteria(c);
		 input.setLevel(DataLevel.XRF_INS);
		JavaRDD<DataContainer> rdd = sparkDataRetrieval.retrieveSparkRDD(input, session);
		Assert.assertEquals(0, rdd.count());
	 
	 }
	
	@Test
	 public void test_retrieveSparkRDDWithFiltering_recordFound() throws Exception{
		 DataRetrivalInput input=new DataRetrivalInput();
		 DataValue<String> xrfIsinVal=new DataValue<>();
			String xrfIsin="BMG2157R1189";
			xrfIsinVal.setValue(LockLevel.RDU, xrfIsin);
			Criteria c1=Criteria.where(xrfIsinAttr).is(xrfIsinVal);
			
			DataValue<String> xrfSedolVal=new DataValue<>();
			String xrfSedol="sedol";
			xrfSedolVal.setValue(LockLevel.RDU, xrfSedol);
			Criteria c2 = Criteria.where(xrfSedolAttr).is(xrfSedolVal);
			
			DataValue<DomainType> xrfExchangeCodeVal=new DataValue<>();
			String xrfExchangeCode="XNYS";
			xrfExchangeCodeVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfExchangeCode));
			Criteria c3=Criteria.where(xrfExchangeCodeAttr).is(xrfExchangeCodeVal);
			
			DataValue<DomainType> xrfSecLinkStatusVal=new DataValue<>();
			String xrfSecLinkStatus="A";
			xrfSecLinkStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfSecLinkStatus));
			Criteria c4=Criteria.where(xrfSecLinkStatusAttr).is(xrfSecLinkStatusVal);
			
			
			DataValue<DomainType> dataSourceValue=new DataValue<>();
			String dataSource="trdse";
			dataSourceValue.setValue(LockLevel.RDU, new DomainType(null, null, dataSource));
			Criteria c5 = Criteria.where(xrfSecLinkDatSourceAttr).is(dataSourceValue);
			
		 Criteria c=c1.andOperator(c2,c3,c4,c5);
		 input.setCriteria(c);
		 input.setLevel(DataLevel.XRF_INS);
		JavaRDD<DataContainer> rdd = sparkDataRetrieval.retrieveSparkRDDWithChildFiltering(input, session);
		Assert.assertEquals(1, rdd.count());
		DataContainer firstContaiuner = rdd.first();
		Assert.assertEquals(1, firstContaiuner.getAllChildDataContainers().size());
	 
	 }
	
	@Test
	 public void test_retrieveSparkRDDWithFiltering_recordNotFound() throws Exception{

		 DataRetrivalInput input=new DataRetrivalInput();
		 DataValue<String> xrfIsinVal=new DataValue<>();
			String xrfIsin="BMG2157R1189";
			xrfIsinVal.setValue(LockLevel.RDU, xrfIsin);
			Criteria c1=Criteria.where(xrfIsinAttr).is(xrfIsinVal);
			
			DataValue<String> xrfSedolVal=new DataValue<>();
			String xrfSedol="sedol";
			xrfSedolVal.setValue(LockLevel.RDU, xrfSedol);
			Criteria c2 = Criteria.where(xrfSedolAttr).is(xrfSedolVal);
			
			DataValue<DomainType> xrfExchangeCodeVal=new DataValue<>();
			String xrfExchangeCode="XNYS";
			xrfExchangeCodeVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfExchangeCode));
			Criteria c3=Criteria.where(xrfExchangeCodeAttr).is(xrfExchangeCodeVal);
			
			DataValue<DomainType> xrfSecLinkStatusVal=new DataValue<>();
			String xrfSecLinkStatus="A";
			xrfSecLinkStatusVal.setValue(LockLevel.RDU, new DomainType(null, null, xrfSecLinkStatus));
			Criteria c4=Criteria.where(xrfSecLinkStatusAttr).is(xrfSecLinkStatusVal);
			
			
			DataValue<DomainType> dataSourceValue=new DataValue<>();
			String dataSource="figi";
			dataSourceValue.setValue(LockLevel.RDU, new DomainType(null, null, dataSource));
			Criteria c5 = Criteria.where(xrfSecLinkDatSourceAttr).is(dataSourceValue);
			
		 Criteria c=c1.andOperator(c2,c3,c4,c5);
		 input.setCriteria(c);
		 input.setLevel(DataLevel.XRF_INS);
		JavaRDD<DataContainer> rdd = sparkDataRetrieval.retrieveSparkRDDWithChildFiltering(input, session);
		Assert.assertEquals(0, rdd.count());
	 }
	
	@Test
	public void test_retrieveSparkDocumentRDD_recordFound() throws Exception{
		DataRetrivalInput input=new DataRetrivalInput();
		
		DataAttribute isin = DataAttributeFactory.getAttributeByNameAndLevel("isin", DataLevel.PROFORMA_INS);
		DataAttribute value = DataAttributeFactory.getAttributeByNameAndLevelAndParent("value", DataLevel.PROFORMA_INS, isin);
		DataValue<String> val = new DataValue<>();
		LockLevelInfo noneLockinfo = new LockLevelInfo(LockLevel.NONE);
		val.setValue(LockLevel.NONE, "LU0048587603", noneLockinfo);
		Criteria criteria = Criteria.where(value).is(val);
		
		input.setCriteria(criteria);
		input.setLevel(DataLevel.PROFORMA_INS);
		input.setCollectionName(ProformaConstants.PROFORMA_COLLECTION_PREFIX+"ESMA");
		JavaRDD<Document> rdd = sparkDataRetrieval.retrieveSparkDocumentRDD(input, session);
		Assert.assertEquals(1, rdd.count());
	}
	
	@After
	public void tearDown(){
		mongo.remove(new Query(), repo.getRootClassForLevel(DataLevel.XRF_INS));
		mongo.remove(new Query(), ProformaConstants.PROFORMA_COLLECTION_PREFIX+"ESMA");
		if(session!=null)
		session.close();
	}

}
