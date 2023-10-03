/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	OpenFigiIdentifierMapperImpl.java
 * Author:	Shruti Arora
 * Date:	06-Jul-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.openfigi;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.openfigi.VfsFigiRequestMessage;
import com.smartstreamrdu.util.Constant.OpenFigiContants;

@Component
public class OpenFigiIdentifierMapperImpl implements OpenFigiIdentifierMapper {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 6847219602578544846L;

	/**
	 * This method will accept a OpenFigiIdentifiers Object and name of identifier
	 * it will provide the value of a figi identifier by the name. If there is no identifier present 
	 * with input name, null will be returned.
	 * 
	 * @param figiIdentifiers
	 * @param name
	 * @return
	 */
	@Override
	public Serializable getFigiIdentifierValueByName(VfsFigiRequestMessage figiIdentifiers, String name) {
		if(StringUtils.isEmpty(name) || figiIdentifiers==null) {
			return null;
		}
		return getMappedValue( figiIdentifiers,  name);
	}
	
	private Serializable getMappedValue(VfsFigiRequestMessage figiIdentifiers, String name) {
		
		switch(name) {
		case OpenFigiContants.ISIN:
			return figiIdentifiers.getIsin();
		case OpenFigiContants.SEDOL:
			return figiIdentifiers.getSedol();
		case OpenFigiContants.CUSIP:
			return figiIdentifiers.getCusip();
		case OpenFigiContants.EX_CODE:
			return returnBasedOnMicList(figiIdentifiers);
		case OpenFigiContants.TRADE_CURR:
			return figiIdentifiers.getTradeCurrencyCode();
		default: return null;
		}
	}

	/**
	 * @param figiIdentifiers
	 * @return
	 */
	private Serializable returnBasedOnMicList(VfsFigiRequestMessage figiIdentifiers) {
		if (figiIdentifiers.getMicIndex() == -1) {
			return figiIdentifiers.getExchangeCode();
		} else {
			List<String> fallbackMics = figiIdentifiers.getDependentMics();
			if (CollectionUtils.isEmpty(fallbackMics)) {
				return null;
			}
			return fallbackMics.get(figiIdentifiers.getMicIndex());
		}
	}

	@Override
	public void setFigiIdentifierValueByName(VfsFigiRequestMessage figiIdentifiers, String name, String value) {
		if(StringUtils.isEmpty(name) || figiIdentifiers==null) {
			return;
		}
		setMappedValue(figiIdentifiers, name, value);	
	}

	private void setMappedValue(VfsFigiRequestMessage figiIdentifiers, String name, String value) {

		switch(name) {
		case OpenFigiContants.ISIN:
			figiIdentifiers.setIsin(value);
			break;
		case OpenFigiContants.SEDOL:
			figiIdentifiers.setSedol(value);
			break;
		case OpenFigiContants.CUSIP:
			figiIdentifiers.setCusip(value);
			break;
		case OpenFigiContants.EX_CODE:
			figiIdentifiers.setExchangeCode(value);
			break;
		case OpenFigiContants.TRADE_CURR:
			figiIdentifiers.setTradeCurrencyCode(value);
			break;
		default: return;
		}
	}
}
