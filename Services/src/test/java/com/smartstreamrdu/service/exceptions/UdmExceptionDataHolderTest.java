package com.smartstreamrdu.service.exceptions;

import org.junit.Test;
import org.springframework.util.Assert;

import com.smartstreamrdu.service.exceptions.UdmExceptionDataHolder.Builder;

public class UdmExceptionDataHolderTest {

	@Test
	public void testBuilder() {
		
		Builder builder = UdmExceptionDataHolder.builder();
		
		Assert.notNull(builder, "UdmExceptionDataHolder builder method returned null.");
	}

}
