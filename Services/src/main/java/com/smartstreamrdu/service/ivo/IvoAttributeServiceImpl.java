/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoAttributeServiceImpl.java
 * Author : SaJadhav
 * Date : 03-Apr-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;

/**
 * @author SaJadhav
 *
 */
@Component
public class IvoAttributeServiceImpl implements IvoAttributeService {

	private final Map<DataLevel, DataLevel> sdLevelVsIvoLevelMap = new EnumMap<>(DataLevel.class);

	@PostConstruct
	public void populatesdLevelVsIvoLevelMap() {
		sdLevelVsIvoLevelMap.put(DataLevel.INS, DataLevel.IVO_INS);
		sdLevelVsIvoLevelMap.put(DataLevel.SEC, DataLevel.IVO_SEC);
		sdLevelVsIvoLevelMap.put(DataLevel.LE, DataLevel.IVO_LE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.ivo.IvoAttributeService#
	 * getIvoAttributeForSdAttribute(com.smartstreamrdu.domain.DataAttribute)
	 */
	@Override
	public DataAttribute getIvoAttributeForSdAttribute(DataAttribute sdAttribute) {
		return DataAttributeFactory.getAttributeByNameAndLevel(sdAttribute.getAttributeName(),
				sdLevelVsIvoLevelMap.get(sdAttribute.getAttributeLevel()));
	}

}
