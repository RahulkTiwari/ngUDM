/*******************************************************************
 *
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility
 * All rights reserved.
 *
 * File:    EsmaFirdsInstrumentInactivation.java
 * Author:    Padgaonkar
 * Date:    03-Mar-2020
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.inactive;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.events.InactiveBean;
import com.smartstreamrdu.exception.UdmBusinessException;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * This implementation is added to cater inActivation of instrument if
 * all securities of corresponding instrument are inActive.
 * 
 * @author Padgaonkar
 *
 */
@Component("esmaFirdsINSInactive")
public class EsmaFirdsInstrumentInactivation implements InactiveService {

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 * @throws UdmTechnicalException 
	 */
	@Override
	public void inactivateIfRequired(InactiveBean inactive) throws UdmBusinessException, UdmTechnicalException {
		InstrumentInactiveUtil.inactiveInstrumentIfAllSecurituesInactive(inactive.getDatasource(),inactive.getDbContainer());
	}
}
