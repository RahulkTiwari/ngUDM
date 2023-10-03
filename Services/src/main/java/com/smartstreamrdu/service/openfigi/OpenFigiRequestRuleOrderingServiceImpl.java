/**
 * Copyright (c) 2009-2023 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : OpenFigiRequestRuleOrderingServiceImpl.java
 * Author :SaJadhav
 * Date : 30-Jan-2023
 */
package com.smartstreamrdu.service.openfigi;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.openfigi.OpenFigiRequestOrderTypeEnum;
import com.smartstreamrdu.commons.openfigi.OpenFigiRequestRuleEnum;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.persistence.cache.UdmSystemPropertiesCache;
import com.smartstreamrdu.util.VfsSystemPropertiesConstant;

import lombok.NonNull;

/**
 * This service gives the open figi rule based on the current rule and 
 * request order type
 * 
 * @author SaJadhav
 *
 */
@Component
public class OpenFigiRequestRuleOrderingServiceImpl implements OpenFigiRequestRuleOrderingService {

	private final Map<String, OpenFigiRequestOrderTypeEnum> micVsRequestOrderMap = new HashMap<>();

	private final Map<OpenFigiRequestOrderTypeEnum, List<OpenFigiRequestRuleEnum>> requestOrderTypevsRequestOrderMap = new EnumMap<>(
			OpenFigiRequestOrderTypeEnum.class);

	@Autowired
	private UdmSystemPropertiesCache systemPropertiesCache;

	@PostConstruct
	public void initializeSystemProperties() {
		initializeRequestOrderVsListMicsMap();
	}

	/**
	 * 
	 */
	private void initializeRequestOrderTypevsRequestOrderMap() {
		for (OpenFigiRequestOrderTypeEnum orderTypeEnum : OpenFigiRequestOrderTypeEnum.values()) {
			Optional<List<String>> listRequestOrderOptional = systemPropertiesCache.getPropertiesValues(
					VfsSystemPropertiesConstant.FIGI_API_REQUEST_ORDER_PREFIX + orderTypeEnum,DataLevel.VFS_SYSTEM_PROPERTIES);
			List<String> requestOrderList = listRequestOrderOptional.orElseThrow(()->new IllegalStateException(VfsSystemPropertiesConstant.FIGI_API_REQUEST_ORDER_PREFIX + orderTypeEnum+" not configured in database"));
			List<OpenFigiRequestRuleEnum> collect = requestOrderList.stream().map(OpenFigiRequestRuleEnum::valueOf).collect(Collectors.toList());
			requestOrderTypevsRequestOrderMap.put(orderTypeEnum, collect);
		}

	}

	/**
	 * Retrieves the system property
	 * figi.api.request.order.mics.<OpenFigiRequestOrderTypeEnum> and creates a map
	 * entry of OpenFigiRequestOrderTypeEnum vs list of Mics
	 */
	private void initializeRequestOrderVsListMicsMap() {
		for (OpenFigiRequestOrderTypeEnum orderTypeEnum : OpenFigiRequestOrderTypeEnum.values()) {
			Optional<List<String>> listMicsOptional = systemPropertiesCache.getPropertiesValues(
					VfsSystemPropertiesConstant.FIGI_API_REQUEST_ORDER_MICS_PREFIX + orderTypeEnum,
					DataLevel.VFS_SYSTEM_PROPERTIES);
			if (listMicsOptional.isPresent()) {
				List<String> listMics = listMicsOptional.get();
				listMics.forEach(mic -> micVsRequestOrderMap.put(mic, orderTypeEnum));
			}
		}
	}

	@Override
	public Optional<OpenFigiRequestRuleEnum> getNextRequestRule(@NonNull OpenFigiRequestRuleEnum currentRequestRule,
			@NonNull OpenFigiRequestOrderTypeEnum requestOrderType) {
		if (requestOrderTypevsRequestOrderMap.isEmpty()) {
			synchronized (this) {
				if (requestOrderTypevsRequestOrderMap.isEmpty()) {
					initializeRequestOrderTypevsRequestOrderMap();
				}
			}
		}
		List<OpenFigiRequestRuleEnum> listRules = requestOrderTypevsRequestOrderMap.get(requestOrderType);
		for (int i = 0; i < listRules.size(); i++) {
			if (currentRequestRule.equals(listRules.get(i)) && listRules.size() > i + 1) {
				return Optional.of(listRules.get(i + 1));
			}
		}
		return Optional.empty();
	}

	@Override
	public OpenFigiRequestOrderTypeEnum getRequestOrderTypeForMic(String mic) {
		if(!micVsRequestOrderMap.containsKey(mic)) {
			return OpenFigiRequestOrderTypeEnum.DEFAULT_ISIN_BASED;
		}
		return micVsRequestOrderMap.get(mic);
	}
}
