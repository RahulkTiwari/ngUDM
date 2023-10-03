/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoContainerAttributeAddHandlerFactory.java
 * Author : SaJadhav
 * Date : 12-Apr-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataLevel;

/**
 * @author SaJadhav
 *
 */
@Component
public class IvoContainerAttributeAdditionHandlerFactory {
	
	@Autowired
	private SdContainerAttributeAdditionHandler sdAttributeAddHandler;
	@Autowired
	private SdIvoContainerAttributeAdditionHandler sdIvoAttributeAddHandler;
	@Autowired
	private SdLeContainerAttributeAdditionHandler sdLeAttributeAddHandler;
	
  public IvoContainerAttributeAdditionHandler getIvoContainerAttributeAdditionHandler(DataLevel level){
	switch (level) {
	case INS:
	case SEC:
		return sdAttributeAddHandler;
	case IVO_INS:
	case IVO_SEC:
		return sdIvoAttributeAddHandler;
	case LE:
		return sdLeAttributeAddHandler;
	default:
		break;
	}
	return null;
  }

}
