package com.smartstreamrdu.api.response;

import java.util.Arrays;
import java.util.List;

import org.junit.experimental.theories.DataPoint;

import com.smartstreamrdu.bean.test.AbstractJavaBeanTest;

public class ApiResponseTest  extends AbstractJavaBeanTest<ApiResponse> {
	
	
	@DataPoint
	public static List<ApiResponse> getDataPoints() {
		ResponseContent c = createContent1();
		
		ApiResponse r1 = new ApiResponse();
		r1.addResponseCodeNMessage("S2000", "Success");
		r1.addContent(c);
		
		ResponseContent c2 = createContent2();
		
		ApiResponse r2 = new ApiResponse();
		r2.addResponseCodeNMessage("W2000", "ISIN NOT FOUND");
		r2.addContent(c2);

		return Arrays.asList(r1, r2);
	}

	private static ResponseContent createContent2() {
		ResponseContent c2 = new ResponseContent();
		c2.put("data", "content2");
		return c2;
	}

	private static ResponseContent createContent1() {
		return new ResponseContent();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Prafab<ResponseContent> prefabValues() {
		return new Prafab<ResponseContent>(ResponseContent.class, createContent1(), createContent2());
	}

	@Override
	protected Class<ApiResponse> getBeanClass() {
		return ApiResponse.class;
	}


}
