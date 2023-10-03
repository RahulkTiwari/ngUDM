package com.smartstreamrdu.service.kafka;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.kafka.KafkaOffsetMessage;
import com.smartstreamrdu.persistence.repository.service.KafkaMessageRepositoryService;
import com.smartstreamrdu.persistence.service.SpringUtil;

/**
 * @author Shreepad Padgaonkar.
 *
 */
@Component
public class OffsetManager {

	@Autowired
	private  MongoTemplate mongoTemplate;

	@Autowired
	private KafkaMessageRepositoryService messageRepository;


	private void ensureMongoTemplate() {
		if (mongoTemplate == null) {
			mongoTemplate = SpringUtil.getBean(MongoTemplate.class);
		}
	}

	private void ensureKafkaMessageRepositoryService() {
		if (messageRepository == null) {
			messageRepository = SpringUtil.getBean(KafkaMessageRepositoryService.class);
		}
	}

	public Query getQueryObject(String topic, int partition) {

		return  new Query(Criteria.where(com.smartstreamrdu.util.Constant.KafkaConstant.TOPIC).is(topic)
				.andOperator(Criteria.where(com.smartstreamrdu.util.Constant.KafkaConstant.PARTITION).is(partition)));

	}

	public void saveOffsetInExternalStore(String topic, int partition, long offset) {

		ensureMongoTemplate();
		ensureKafkaMessageRepositoryService();
		Query searchQuery = getQueryObject(topic, partition);

		Update update = new Update();
		update.set(com.smartstreamrdu.util.Constant.KafkaConstant.PARTITION, partition);
		update.set(com.smartstreamrdu.util.Constant.KafkaConstant.OFFSET, offset);
		
		
		mongoTemplate.findAndModify(searchQuery, update, FindAndModifyOptions.options().upsert(true), KafkaOffsetMessage.class);
	}

	public long readOffsetFromExternalStore(String topic, int partition) {

		ensureMongoTemplate();
		ensureKafkaMessageRepositoryService();
		Query searchQuery = getQueryObject(topic, partition);

		List<KafkaOffsetMessage> msglist = mongoTemplate.find(searchQuery, KafkaOffsetMessage.class);
		if (CollectionUtils.isNotEmpty(msglist)) {
			KafkaOffsetMessage message =  msglist.get(0);
			return message.getOffset() + 1;

		} else {
			KafkaOffsetMessage message = new KafkaOffsetMessage(topic, partition, 0);
			messageRepository.saveKafkaMessage(message);
			return 0;
		}

	}
	
	public List<KafkaOffsetMessage> getAllOffset(String topic){
		
		Query searchQuery = new Query(Criteria.where(com.smartstreamrdu.util.Constant.KafkaConstant.TOPIC).is(topic));
		return mongoTemplate.find(searchQuery, KafkaOffsetMessage.class);
		
	}
	
	public void saveOffsetInExternalStore(List<KafkaOffsetMessage> offsets) {
		
		if(CollectionUtils.isEmpty(offsets)){
			return;
		}
		
		for(KafkaOffsetMessage offset : offsets){
			saveOffsetInExternalStore(offset.getTopic(), offset.getPartition(), offset.getOffset());
		}
		
	}
	
	
	
}
