/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	DataContainerCloneService.java
 * Author:	Jay Sangoi
 * Date:	15-Apr-2019
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.domain;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.exception.UdmBaseException;
import com.smartstreamrdu.exception.UdmValidationException;

/**
 * Service related to Data Container cloning. It has operation like create minimum attribute data container from existing data container etc
 * @author Jay Sangoi
 *
 */
public interface DataContainerCloneService {

	/**
	 * This will create an Instrument Data Container with minimum attribute  The minimum attribute is defined by {@link DataContainerCloneUtil.INSTRUMENT_MIN_ATT}
	 * @param container
	 * @return
	 * @throws UdmValidationException 
	 */
	DataContainer createMinimumAttributeInstrumentDC(DataContainer container) throws UdmBaseException;
	
	/**
	 * This will create an Instrument Data Container with minimum attribute + flagActive as inactive. The minimum attribute is defined by {@link DataContainerCloneUtil.INSTRUMENT_MIN_ATT}
	 * @param container
	 * @return
	 * @throws UdmValidationException 
	 */
	DataContainer createMinimumAttributeInactiveInstrumentDC(DataContainer container) throws UdmBaseException;
	
	/**
	 * This will create an Instrument Data Container with minimum attribute + flagActive as active . The minimum attribute is defined by {@link DataContainerCloneUtil.INSTRUMENT_MIN_ATT}
	 * @param container
	 * @return
	 * @throws UdmValidationException 
	 */
	DataContainer createMinimumAttributeActiveInstrumentDC(DataContainer container) throws UdmBaseException;

	/**
	 * This will create an Security Data Container with minimum attribute. The minimum attribute is defined by {@link DataContainerCloneUtil.SECURTY_MIN_ATT}
	 * @param container
	 * @return
	 * @throws UdmValidationException 
	 */
	DataContainer createMinimumAttributeSecurityDC(DataContainer container) throws UdmBaseException;

	
	/**
	 * This will create an Security Data Container with minimum attribute + flagActive as inactive. The minimum attribute is defined by {@link DataContainerCloneUtil.SECURTY_MIN_ATT}
	 * @param container
	 * @param dtDs 
	 * @return
	 * @throws UdmValidationException 
	 */
	DataContainer createMinimumAttributeInactiveSecurityDC(DataContainer container, DomainType dtDs) throws UdmBaseException;
	
	/**
	 * This will create an Security Data Container with minimum attribute + flagActive as inactive. The minimum attribute is defined by {@link DataContainerCloneUtil.SECURTY_MIN_ATT}
	 * @param container
	 * @return
	 * @throws UdmValidationException 
	 */
	DataContainer createMinimumAttributeActiveSecurityDC(DataContainer container,DomainType dataSource) throws UdmBaseException;


}
