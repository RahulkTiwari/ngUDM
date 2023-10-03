package com.smartstreamrdu.service.domain;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.DomainType;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ServiceTestConfiguration.class })
public class DomainLookupServiceImplTest {

	@Autowired
	private DomainLookupService impl;

	public final static DataAttribute INS_Vendor_mapping = DataAttributeFactory
			.getAttributeByNameAndLevel("vendorMappings", DataLevel.DV_DOMAIN_MAP);


	@Test
	public void testGetNormalizedValueForDomainValue() {
		DomainType domainData = new DomainType();
		domainData.setVal("BSE");
		Mockito.when(impl.getNormalizedValueForDomainValue(domainData, "trds", "NSE|Exchange", "exchangeCodes")).thenReturn("XBAH");

		Serializable value = impl.getNormalizedValueForDomainValue(domainData, "trds", "NSE|Exchange", "exchangeCodes");

		Assert.assertEquals("XBAH", value);
	}

	@Test
	public void testGetNormalizedValue() {
		
			DomainType type= new DomainType();
			type.setVal("GSX");
			Mockito.when(impl.getNormalizedValueForDomainValue(type, "trds", null, "exchangeCodes")).thenReturn("XPSX");
			String val=(String) impl.getNormalizedValueForDomainValue(type,"trds",null,"exchangeCodes");
			Assert.assertEquals("XPSX", val);

	}
}
