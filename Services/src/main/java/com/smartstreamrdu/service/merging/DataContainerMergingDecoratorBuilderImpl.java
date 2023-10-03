/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerMergingDecoratorBuilderImpl.java
 * Author:	Jay Sangoi
 * Date:	05-Jun-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.merging;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.persistence.service.SpringUtil;

/**
 * @author Jay Sangoi
 *
 */
@Component
public class DataContainerMergingDecoratorBuilderImpl implements DataContainerMergingDecoratorBuilder{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5309751537089892716L;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.DataContainerMergingDecoratorBuilder#createDataContainerMergingDecorator(com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public DataContainerMergingDecorator createDataContainerMergingDecorator(DataContainer feedContainer,
			List<DataContainer> dbContainers) {
		
		//By default we will have Attribute level merging.
		
		DataContainerMergingDecorator decorator = null;
		
		DataContainerMerging attributemerging = SpringUtil.getBean(AttributeMergingDecorator.class);
		
		Map<DataAttribute, List<DataContainer>> childDataContainers = feedContainer.getChildDataContainers();
		if(childDataContainers != null && !childDataContainers.isEmpty()){
			//For Parent child merging
			decorator = SpringUtil.getBean(ParentChildMergingDecorator.class, attributemerging);
		}
		
		if(CollectionUtils.isNotEmpty(dbContainers) && dbContainers.size() > 1){
			//For Multiple Container mergingg
			decorator = createMultiContainerDecorator(decorator, attributemerging);
		}
		
		if(decorator == null){
			decorator = SpringUtil.getBean(DefaultDataContainerMergingDecorator.class, attributemerging);
		}
		
		
		return decorator;
	}

	/**
	 * @param decorator
	 * @param attributemerging
	 * @return
	 */
	private DataContainerMergingDecorator createMultiContainerDecorator(DataContainerMergingDecorator decorator,
			DataContainerMerging attributemerging) {
		if(decorator == null){
			decorator =  SpringUtil.getBean(MultipleContainerMergingDecorator.class, attributemerging);
		}
		else{
			decorator =  SpringUtil.getBean(MultipleContainerMergingDecorator.class, decorator);
		}
		return decorator;
	}

}
