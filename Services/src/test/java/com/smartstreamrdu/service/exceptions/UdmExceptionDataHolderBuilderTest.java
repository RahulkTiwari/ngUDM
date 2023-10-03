package com.smartstreamrdu.service.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.smartstreamrdu.service.exceptions.UdmExceptionDataHolder.Builder;


public class UdmExceptionDataHolderBuilderTest {

	@Test
	public void testBuild() {
		
		Builder builder = UdmExceptionDataHolder.builder();
		
		Map<String, String> customFields = new HashMap<>();
		
		
		customFields.put("Affected RDU Instrument ID", "RDUIN12345");
		customFields.put("Affected RDU Security IDs", "RDUSEC12345");
		customFields.put("Resolution Url", "Some URL");
		customFields.put("Exchange(s)(Primary)", "XBOM");
		
		builder.withCustomFieldValues(customFields);
		
		builder.withCategory("Validation").withDescription("Isin Mismatch")
				.withExceptionType("Value Mismatch Isin").withIncidentId("INC12345")
						.withPriority("High").withSubCategory("Value Mismatch");
		
		UdmExceptionDataHolder dataHolder  = builder.build();
		
		Assert.assertNotNull(dataHolder);
		
		Assert.assertEquals("Validation", dataHolder.getCategory());
		Assert.assertEquals("Isin Mismatch", dataHolder.getDescription());
		Assert.assertEquals("Value Mismatch Isin", dataHolder.getExceptionType());
		Assert.assertEquals("INC12345", dataHolder.getIncidentId());
		Assert.assertEquals("High", dataHolder.getPriority());
		Assert.assertEquals("Value Mismatch", dataHolder.getSubCategory());
	}

}