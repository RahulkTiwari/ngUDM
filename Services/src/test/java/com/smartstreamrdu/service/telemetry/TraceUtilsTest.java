/**
 * 
 */
package com.smartstreamrdu.service.telemetry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.ImmutableList;
import com.smartstreamrdu.domain.TraceInfo;
import com.smartstreamrdu.service.telemetry.TraceUtils.TextMapGetterImplementation;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * @author Akshay Gehi
 *
 */
public class TraceUtilsTest {
	

	private static final String TEST_KEY = "test";
	private static final String TEST_VALUE = "testValue";
	
	TextMapGetterImplementation impl = new TextMapGetterImplementation();

	TraceInfo traceInfo;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		traceInfo = new TraceInfo();
		traceInfo.put(TEST_KEY, TEST_VALUE);
		
	}

	@Test(expected = NullPointerException.class)
	public void testTextMapGetter_null() {
		impl.keys(null);
	}
	
	@Test
	public void testTextMapGetter() {
		Iterable<String> keys = impl.keys(traceInfo);
		ImmutableList<String> keyList = ImmutableList.copyOf(keys);
		assertEquals(1, keyList.size());
		assertTrue(keyList.contains(TEST_KEY));
		
		String v = impl.get(traceInfo, TEST_KEY);
		assertEquals(TEST_VALUE, v);
	}
	
	@Test
	public void testGetTracer() throws Exception {
		
		Field field = TraceUtils.class.getDeclaredField("tracer");
		field.setAccessible(true);
		ReflectionUtils.setField(field, null, null);
		
		Tracer tracer = TraceUtils.getTracer();
		assertNotNull(tracer);
		
		assertSame(tracer, TraceUtils.getTracer());
	}

	@Test
	public void testGetTextMapPropagator() {
		TextMapPropagator p = TraceUtils.getTextMapPropagator();
		assertNotNull(p);
	}

	@Test
	public void testContextToTraceInfo() {
		TraceInfo p = TraceUtils.contextToTraceInfo();
		assertNotNull(p);
	}

	@Test
	public void testTraceInfoToContext() {
		Context p = TraceUtils.traceInfoToContext(traceInfo);
		assertNotNull(p);
	}
	
	@Test(expected = NullPointerException.class)
	public void testTraceInfoToContext_null() {
		TraceUtils.traceInfoToContext(null);
	}
}
