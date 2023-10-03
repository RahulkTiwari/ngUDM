/**
 * 
 */
package com.smartstreamrdu.service.merging;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.util.Constant;

/**
 * 
 * Nested array comparator for ENS dataLevel
 * @author RKaithwas
 *
 */
@Component("ENNestedComparator")
public class ENNestedArrayComparator extends AbstractNestedArrayComparator implements NestedArrayComparator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7216073567730941963L;

	/**
	 *  * Nested array comparator for ENS dataLevel

	 */
	@Override
	public DataRow compare(NestedArrayComparatorInputPojo inputPojo) {
		DataAttribute arrayAttribute = inputPojo.getArrayAttribute();
		DataRow feed = inputPojo.getFeed();
		DataRowIterator dbIterator = inputPojo.getDbIterator();
		DataContainer dbDataContianer = inputPojo.getDbDataContainer();
		
		DomainType dataSourceValue = (DomainType) dbDataContianer.getAttributeValueAtLevel(LockLevel.FEED,
				DataAttributeFactory.getAttributeByNameAndLevel(Constant.ListenerConstants.dataSource,
						dbDataContianer.getLevel().getRootLevel()));
		
		DataRow row = null;
		if(arrayAttribute.getDataType().equals(DataType.NESTED_ARRAY)){
			
			while(dbIterator.hasNext()){
				DataRow dbRow = dbIterator.next();
				if(allKeysEquate(feed, dbRow, arrayAttribute, dataSourceValue)){
					row = dbRow;
					break;
				}
			}
		}
		return row;
	}


}
