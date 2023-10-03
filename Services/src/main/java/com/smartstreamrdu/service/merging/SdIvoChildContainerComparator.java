/**
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: SdIvoChildContainerComparator.java
 * Author : SaJadhav
 * Date : 14-Feb-2019
 * 
 */
package com.smartstreamrdu.service.merging;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.domain.ReferenceId;
import com.smartstreamrdu.domain.RelationType;
import com.smartstreamrdu.util.IvoConstants;

/**
 * @author SaJadhav
 *
 */
@Component("IVOChildComparator")
public class SdIvoChildContainerComparator implements ChildContainerComparator {

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.ChildContainerComparator#compare(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public DataContainer compare(DataContainer parent, DataContainer source, List<DataContainer> destination)
			throws Exception {

		if (source == null) {
			throw new IllegalArgumentException(
					"Exception while merging SD IVO data containers. Neither the feed container can be  empty");
		}
		if(CollectionUtils.isEmpty(destination)){
			return null;
		}
		DataContainer outcome = null;

		DataAttribute secRelAttribute=DataAttributeFactory.getAttributeByNameAndLevel(IvoConstants.SECURITY_RELATIONS, DataLevel.IVO_SEC);

		for(DataContainer dbContainer : destination) {
			
			ReferenceId dbRefId = getIvoSecRelationRefId(dbContainer,secRelAttribute);
			ReferenceId sourceRefId=getIvoSecRelationRefId(source,secRelAttribute);

			if (dbRefId!=null && dbRefId.equals(sourceRefId)) {
				// sufficient for proving equality of the containers.
				outcome = dbContainer;
				break;
			}
		}
		return outcome;
	}

	/**
	 * @param dbContainer
	 * @param secRelAttribute
	 */
	private ReferenceId getIvoSecRelationRefId(DataContainer dbContainer, DataAttribute secRelAttribute) {
		DataRowIterator iterator=new DataRowIterator(dbContainer, secRelAttribute);
		ReferenceId output=null;
		while(iterator.hasNext()){
			DataRow dataRow = iterator.next();
			String relaTionTypeVal=(String) dataRow.getAttributeValueAtLevel(LockLevel.RDU, 
					DataAttributeFactory.getRelationTypeAttribute(secRelAttribute));
			if(RelationType.IVO.name().equals(relaTionTypeVal)){
				output=(ReferenceId) dataRow.getAttributeValueAtLevel(LockLevel.RDU, 
						DataAttributeFactory.getRelationRefIdAttribute(secRelAttribute));
				break;
			}
		}
		return output;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.ChildContainerComparator#compare(com.smartstreamrdu.domain.DataContainer, com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public boolean compare(DataContainer source, DataContainer destination) {
		throw new UnsupportedOperationException();
	}

}
