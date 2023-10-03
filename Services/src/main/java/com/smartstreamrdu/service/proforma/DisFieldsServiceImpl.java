/**
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File : DisFieldsServiceImpl.java
 * Author :SaJadhav
 * Date : 19-Feb-2020
 */
package com.smartstreamrdu.service.proforma;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DisDataType;
import com.smartstreamrdu.domain.proforma.ProformaDistributionServiceEnum;
import com.smartstreamrdu.persistence.domain.Field;
import com.smartstreamrdu.persistence.repository.DisFieldsRepository;
import com.smartstreamrdu.rules.DisField;
import com.smartstreamrdu.rules.DisFieldParentMapping;
import com.smartstreamrdu.rules.DisRuleOutput;

import lombok.Data;

/**
 *  Service for retrieval on disFields collection
 * 
 * @author SaJadhav
 *
 */
@Component
public class DisFieldsServiceImpl implements DisFieldsService {
	
	@Autowired
	private DisFieldsRepository disFieldsRepo;
	
	private Map<ProformaDistributionServiceEnum,List<DisField>> disServiceVsListOfDisFieldsMap;
	
	private Map<DisFieldLookUpWrapper,DisField> lookUpWrapperVsDisFieldMap;
	
	private Map<DisFieldLookUpWrapper,DisDataType> lookupWrapperVsDataTypeMap;
	
	
	@PostConstruct
	public void initialize(){
		disServiceVsListOfDisFieldsMap=new EnumMap<>(ProformaDistributionServiceEnum.class);
		lookUpWrapperVsDisFieldMap=new HashMap<>();
		lookupWrapperVsDataTypeMap=new HashMap<>();
		List<DisField> allDisFields = disFieldsRepo.findAll();
		populateCache(allDisFields);
	}

	/**
	 * @param allDisFields
	 */
	private void populateCache(List<DisField> allDisFields) {
		for (DisField disField : allDisFields) {
			List<ProformaDistributionServiceEnum> distributionServices = disField.getDistributionServices();
			addToCacheMaps(distributionServices,disField);
		}
	}

	/**
	 * @param distributionServices
	 * @param disField
	 */
	private void addToCacheMaps(List<ProformaDistributionServiceEnum> distributionServices, DisField disField) {
		for (ProformaDistributionServiceEnum disService : distributionServices) {
			
			disServiceVsListOfDisFieldsMap.computeIfAbsent(disService, k -> new ArrayList<>()).add(disField);
			
			DisRuleOutput ruleOutput = disField.getRuleOutput();
			
			DisFieldParentMapping parent = ruleOutput.getParent();
			String parentDisFieldName=null;
			if(parent!=null) {
				parentDisFieldName=parent.getDisFieldName();
			}
					
			lookUpWrapperVsDisFieldMap.put(new DisFieldLookUpWrapper(ruleOutput.getDisField(),
					parentDisFieldName, disService), disField);
			
			lookupWrapperVsDataTypeMap.put(new DisFieldLookUpWrapper(ruleOutput.getDisField(),null, disService), disField.getRuleOutput().getDataType());
		} 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DisField getDisFieldForProfileField(Field profileField,
			ProformaDistributionServiceEnum distributionService) {
		
		DisFieldParentMapping parent = profileField.getParent();
		String parentDisField=null;
		if(parent!=null) {
			parentDisField=parent.getDisFieldName();
		}
		return lookUpWrapperVsDisFieldMap.get(new DisFieldLookUpWrapper(profileField.getDisFieldName(),
				parentDisField, distributionService));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DisField> getAllDisFieldsForDistributionService(ProformaDistributionServiceEnum distributionService) {
		return disServiceVsListOfDisFieldsMap.get(distributionService);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public DisField getDisFieldByDisFieldNameAndParent(String fieldName, String parentName,ProformaDistributionServiceEnum distributionService) {
		return lookUpWrapperVsDisFieldMap.get(new DisFieldLookUpWrapper(fieldName,
				parentName, distributionService));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DisDataType getDataTypeByAttributeName(String fieldName,
			ProformaDistributionServiceEnum distributionService) {
		DisDataType dataType = lookupWrapperVsDataTypeMap
				.get(new DisFieldLookUpWrapper(fieldName, null, distributionService));
		return dataType != null ? dataType : DisDataType.SIMPLE;
	}
	
	@Data
	private class DisFieldLookUpWrapper {
		private String disFieldName;
		private String parent;
		private ProformaDistributionServiceEnum distributionService;

		public DisFieldLookUpWrapper(String disFieldName, String parent,
				ProformaDistributionServiceEnum distributionService) {
			this.disFieldName=disFieldName;
			this.distributionService=distributionService;
			this.parent=parent;
		}
	}

}
