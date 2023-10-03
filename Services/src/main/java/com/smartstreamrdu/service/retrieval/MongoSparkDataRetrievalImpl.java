package com.smartstreamrdu.service.retrieval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.PersistenceRoot;
import com.smartstreamrdu.exception.UdmTechnicalException;
import com.smartstreamrdu.persistance.filter.DataContainerFilterService;
import com.smartstreamrdu.persistence.mongodb.codecs.CodecRegistryHelper;
import com.smartstreamrdu.persistence.retrival.DataRetrivalInput;
import com.smartstreamrdu.persistence.retrival.mongodb.MongoCriteriaConversion;
import com.smartstreamrdu.persistence.service.AttributePersistenceMapperService;
import com.smartstreamrdu.persistence.service.PersistenceEntityRepository;
import com.smartstreamrdu.persistence.service.SpringMongoConversionService;
import com.smartstreamrdu.persistence.service.SpringUtil;
import com.smartstreamrdu.service.spark.MongoSparkUtil;

@Component
public class MongoSparkDataRetrievalImpl implements MongoSparkDataRetrieval {

	private static final long serialVersionUID = 816968982198395696L;

	private static final Logger _logger = LoggerFactory.getLogger(MongoSparkDataRetrievalImpl.class);

	@Autowired
	private transient SpringMongoConversionService conversionService;

	@Autowired
	private MongoCriteriaConversion criteriaConversion;

	@Autowired
	private transient MongoSparkUtil mongoSparkUtil;

	@Autowired
	private transient DataContainerFilterService filterService;

	@Autowired
	private PersistenceEntityRepository entityRepository;

	private SpringMongoConversionService getConversionService() {
		if (conversionService == null) {
			conversionService = SpringUtil.getBean(SpringMongoConversionService.class);
		}
		return conversionService;
	}

	private DataContainerFilterService getContainerFilterService() {
		if (filterService == null) {
			filterService = SpringUtil.getBean(DataContainerFilterService.class);
		}
		return filterService;
	}

	@Override
	public JavaRDD<DataContainer> retrieveSparkRDD(DataRetrivalInput input, SparkSession sparkSession)
			throws UdmTechnicalException {
		return retrieveSparkRddInternal(input, sparkSession, false);
	}


	@Override
	public JavaRDD<DataContainer> retrieveSparkRDDWithChildFiltering(DataRetrivalInput input, SparkSession sparkSession)
			throws UdmTechnicalException {
		return retrieveSparkRddInternal(input, sparkSession, true);
	}

	private JavaRDD<DataContainer> retrieveSparkRddInternal(DataRetrivalInput input, SparkSession sparkSession,
			final boolean filterChilds) throws UdmTechnicalException {
		if (input == null || input.getCriteria() == null) {
			return null;
		}

		Criteria mongoCriteria = criteriaConversion.createMongoCriteria(input.getCriteria());
		String collectionName=null;
		final Class<PersistenceRoot> rootClassForLevel = entityRepository.getRootClassForLevel(input.getLevel());
		
		if(StringUtils.isNotEmpty(input.getCollectionName())){
			collectionName=input.getCollectionName();//required for proforma dynamic collection name
		}else{
			collectionName=rootClassForLevel.getSimpleName();
		}
		
		JavaMongoRDD<Document> javaMongoRdd = retrieveSparkRddInternalWithCriteria(mongoCriteria, sparkSession,collectionName);

		if (javaMongoRdd == null)
			return null;

		return javaMongoRdd.mapPartitions(new FlatMapFunction<Iterator<Document>, DataContainer>() {

			private static final long serialVersionUID = -6005071051229511020L;

			@Override
			public Iterator<DataContainer> call(Iterator<Document> t) throws Exception {

				AttributePersistenceMapperService mapperService = SpringUtil.getBean(AttributePersistenceMapperService.class);

				List<DataContainer> conList = new ArrayList<>();
				while (t.hasNext()) {
					// filter based on additional criteria as provided in input
					List<DataContainer> containers = convertRddToDataContainer(t, mapperService, rootClassForLevel);
					if (filterChilds) {
						conList.addAll(getContainerFilterService().filterDataContainerRecords(containers,
								input.getCriteria(),input.getOptions().shouldPreserveNestedArrayAttributesInFiltering(), input.getOptions().shouldNegateCriteriaCondition()));
					} else {
						conList.addAll(containers);
					}
				}
				_logger.info("DataContainer retrieved:{}", conList.size());
				return conList.iterator();
			}

			private List<DataContainer> convertRddToDataContainer(Iterator<Document> t, AttributePersistenceMapperService mapperService, Class<PersistenceRoot> rootClassForLevel) throws UdmTechnicalException {
				Document next = t.next();
				PersistenceRoot convertedObject = getConversionService().convert(next, rootClassForLevel);
				return mapperService.convertPersistenceToDataContainer(convertedObject);
			}
		});
	}

	private JavaMongoRDD<Document> retrieveSparkRddInternalWithCriteria(Criteria criteria, SparkSession sparkSession,String collectionName) {

		if (criteria == null) {
			return null;
		}

		MongoSpark mongoSpark = mongoSparkUtil.getMongoSpark(collectionName, sparkSession);
		JavaMongoRDD<Document> rdd = mongoSpark.toJavaRDD();
		final List<Document> bsonPipeline = getBsonPipeline(criteria);
		JavaMongoRDD<Document> javaMongoRdd = rdd.withPipeline(bsonPipeline);
		assert (javaMongoRdd != null) : "Pipeline returned null";
		return javaMongoRdd;
	}

	/**
	 * As we are using upgraded jar of Mongo-Spark-Connector Jar_2.12-3.0.0.we have found
	 * there is no support for customCodec in com.mongodb.spark.rdd.MongoRDD-(toBsonDocument Method).As a
	 * workaround we have encodec(using custom codec)our bson object at our end first. convert
	 * it into StringJson & then again convert into BsonDocument & passed to query.
	 * Now BsonDocument doesn't consist of any custom classes & query works.
	 * Details about change Api is mentioned in Jira -https://jira.smartstream-stp.com/browse/UDM-45812
	 * (please refer comment section)
	 * @param criteria
	 * @return
	 */
	private List<Document> getBsonPipeline(Criteria criteria) {
		final List<Document> bsonPipeline = new ArrayList<>();
		Document document = Aggregation.match(criteria).toDocument(Aggregation.DEFAULT_CONTEXT);
		DocumentCodec codec = new DocumentCodec(CodecRegistryHelper.getCodecRegistry());
		String jsonDoc = document.toJson(codec);
		bsonPipeline.add(Document.parse(jsonDoc));
		return bsonPipeline;
	}
	
	/**
	 * Returns bson pipeline with sort and 
	 * limit operations
	 * 
	 * @param criteria
	 * @param sortOperation
	 * @param limitOperation
	 * @return
	 */
	private List<Document> getBsonPipelineWithSortAndLimit(Criteria criteria, SortOperation sortOperation, LimitOperation limitOperation) {
		final List<Document> bsonPipeline = new ArrayList<>();
		Document document = Aggregation.match(criteria).toDocument(Aggregation.DEFAULT_CONTEXT);
		DocumentCodec codec = new DocumentCodec(CodecRegistryHelper.getCodecRegistry());
		String jsonDoc = document.toJson(codec);
		String sortJsonDoc = sortOperation.toDocument(Aggregation.DEFAULT_CONTEXT).toJson(codec);
		String limitJsonDoc = limitOperation.toDocument(Aggregation.DEFAULT_CONTEXT).toJson(codec);
		bsonPipeline.add(Document.parse(jsonDoc));
		bsonPipeline.add(Document.parse(sortJsonDoc));
		bsonPipeline.add(Document.parse(limitJsonDoc));
		return bsonPipeline;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.retrieval.MongoSparkDataRetrieval#retrieveSparkDocumentRDD(com.smartstreamrdu.persistence.retrival.DataRetrivalInput, org.apache.spark.sql.SparkSession)
	 */
	@Override
	public JavaMongoRDD<Document> retrieveSparkDocumentRDD(DataRetrivalInput input, SparkSession sparkSession)
			throws UdmTechnicalException {
		Criteria mongoCriteria = criteriaConversion.createMongoCriteria(input.getCriteria());
		
		return retrieveSparkRddInternalWithCriteria(mongoCriteria, sparkSession, input.getCollectionName());

	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.retrieval.MongoSparkDataRetrieval#retrieveSparkRDD(org.springframework.data.mongodb.core.query.Criteria, org.apache.spark.sql.SparkSession, java.lang.String)
	 */
	@Override
	public JavaMongoRDD<Document> retrieveSparkRDD(Criteria criteria, SparkSession sparkSession, String collectionName)
			throws UdmTechnicalException {
		return retrieveSparkRddInternalWithCriteria(criteria, sparkSession, collectionName);
	}

	/**
	 *
	 */
	@Override
	public JavaMongoRDD<Document> retrieveSparkRddWithLimitAndSort(Criteria criteria, SparkSession session,
			String collectionName, SortOperation sortOperation, LimitOperation limitOperation) {
		if (criteria == null) {
			return null;
		}

		MongoSpark mongoSpark = mongoSparkUtil.getMongoSpark(collectionName, session);
		JavaMongoRDD<Document> rdd = mongoSpark.toJavaRDD();
		final List<Document> bsonPipeline = getBsonPipelineWithSortAndLimit(criteria, sortOperation, limitOperation);
		JavaMongoRDD<Document> javaMongoRdd = rdd.withPipeline(bsonPipeline);
		assert (javaMongoRdd != null) : "Pipeline returned null";
		return javaMongoRdd;
	}
	
	

}
