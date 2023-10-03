/**
 * 
 */
package com.smartstreamrdu.service.telemetry;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import com.smartstreamrdu.domain.DataLevel;
import com.smartstreamrdu.domain.Record;
import com.smartstreamrdu.domain.RecordRawData;
import com.smartstreamrdu.domain.TraceInfo;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;
import com.smartstreamrdu.util.Constant;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import lombok.Getter;

/**
 * 
 * @author Akshay Gehi
 *
 */
@RunWith(value = Parameterized.class)
@ActiveProfiles("junit")
@ContextConfiguration(classes = MongoConfig.class)
public class RecordTelemetryTest {
	
	private static final Serializable VALUE1 = "value1";
	private static final Serializable VALUE2 = "value2";
	
	private static Field FIELD_TRACER;

	private final Record record = getTestRecord();
	private TestContextManager testContextManager;
	
	private static boolean recording;
	
	private static TestTracer currentTracer;
	
	public RecordTelemetryTest(Boolean recoding) {
		RecordTelemetryTest.recording = recoding;
	}
	
	@Parameterized.Parameters(name = "Recoding: {0}")
	public static Iterable<Boolean> data() {
		return Arrays.asList(Boolean.TRUE, Boolean.FALSE );
	}
	
	public static Record getTestRecord() {
		Record record1 = new Record();

		record1.setLevel(DataLevel.INS.name());
		record1.setData(new JSONObject());
		record1.setAttribute("$.Key1", VALUE1);
		record1.setAttribute("$.Key4", " ");
		record1.setAttribute("$.Key6", null);
		record1.setAttribute("$.Key7", 12345);
		record1.setAttribute("$.Key8", true);

		RecordRawData rd = record1.getRecordRawData();
		rd.setRawData(new JSONObject());
		rd.setRawAttribute("$.Key2", VALUE2);
		
		TraceInfo traceInfo = new TraceInfo();
		record1.setTraceInfo(traceInfo);
		return record1;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setup() throws Exception {
        this.testContextManager = new TestContextManager(getClass());
        this.testContextManager.prepareTestInstance(this);

		FIELD_TRACER = TraceUtils.class.getDeclaredField("tracer");
		ReflectionUtils.setField(FIELD_TRACER, TraceUtils.class, new TestTracer());
	}

	@AfterClass
	public static void teardownClass() throws Exception {
		ReflectionUtils.setField(FIELD_TRACER, TraceUtils.class, null);
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		currentTracer = null;
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.telemetry.RecordTelemetry#addSpans(java.util.List, java.lang.String)}.
	 */
	@Test
	public void testAddSpans() {
		RecordTelemetry.addSpans(List.of(record), Constant.CrossRefConstants.DS_REUTERS);
		assertEquals("Parsing", currentTracer.spanBuilder.spanName);
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.telemetry.RecordTelemetry#addParsingSpanWithParent(com.smartstreamrdu.domain.Record, java.lang.String)}.
	 */
	@Test
	public void testAddParsingSpanWithParent() {
		RecordTelemetry.addParsingSpanWithParent(record, Constant.CrossRefConstants.DS_REUTERS);
		assertEquals("Parsing", currentTracer.spanBuilder.spanName);
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.telemetry.RecordTelemetry#addParsingSpan(com.smartstreamrdu.domain.Record, java.lang.String)}.
	 */
	@Test
	public void testAddParsingSpan() {
		RecordTelemetry.addParsingSpan(record, Constant.CrossRefConstants.DS_REUTERS);
		assertEquals("Parsing", currentTracer.spanBuilder.spanName);
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.telemetry.RecordTelemetry#addDecodedSpan(com.smartstreamrdu.domain.Record, java.lang.String)}.
	 */
	@Test
	public void testAddDecodedSpan() {
		RecordTelemetry.addDecodedSpan(record, Constant.CrossRefConstants.DS_REUTERS);
		assertEquals("Decoded", currentTracer.spanBuilder.spanName);
	}

	@Test
	public void testAddDecodedSpan_null() {
		RecordTelemetry.addDecodedSpan(null, Constant.CrossRefConstants.DS_REUTERS);
	}

	/**
	 * Test method for {@link com.smartstreamrdu.service.telemetry.RecordTelemetry#addDeltaRejectEvent(com.smartstreamrdu.domain.Record)}.
	 */
	@Test
	public void testAddDeltaRejectEvent() {
		RecordTelemetry.addDeltaRejectEvent(record);
		assertEquals("Delta Processing", currentTracer.spanBuilder.spanName);
	}
	
	@Test
	public void testAddDeltaRejectEvent_null() {
		RecordTelemetry.addDeltaRejectEvent(null);
		// no exception to be raised
	}

	private static class TestTracer implements Tracer {
		
		TestSpanBuilder spanBuilder;

		private TestTracer() {
			currentTracer = this;
		}
		
		@Override
		public SpanBuilder spanBuilder(String spanName) {
			spanBuilder = new TestSpanBuilder(spanName);
			return spanBuilder;
		}
		
	}
	
	private static class TestSpanBuilder implements SpanBuilder {

		@Getter 
		private String spanName;

		public TestSpanBuilder(String spanName) {
			this.spanName = spanName;
		}

		@Override
		public SpanBuilder setParent(Context context) {
			return this;
		}

		@Override
		public SpanBuilder setNoParent() {
			return this;
		}

		@Override
		public SpanBuilder addLink(SpanContext spanContext) {
			return this;
		}

		@Override
		public SpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
			return this;
		}

		@Override
		public SpanBuilder setAttribute(String key, String value) {
			return this;
		}

		@Override
		public SpanBuilder setAttribute(String key, long value) {
			return this;
		}

		@Override
		public SpanBuilder setAttribute(String key, double value) {
			return this;
		}

		@Override
		public SpanBuilder setAttribute(String key, boolean value) {
			return this;
		}

		@Override
		public <T> SpanBuilder setAttribute(AttributeKey<T> key, T value) {
			return this;
		}

		@Override
		public SpanBuilder setSpanKind(SpanKind spanKind) {
			return this;
		}

		@Override
		public SpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
			return this;
		}

		@Override
		public Span startSpan() {
			return new TestSpan(spanName);
		}
		
	}
	
	private static class TestSpan implements Span {

		@Getter
		private String spanName;

		public TestSpan(String spanName) {
			this.spanName = spanName;
		}

		@Override
		public <T> Span setAttribute(AttributeKey<T> key, T value) {
			return this;
		}

		@Override
		public Span addEvent(String name, Attributes attributes) {
			return this;
		}

		@Override
		public Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
			return this;
		}

		@Override
		public Span setStatus(StatusCode statusCode, String description) {
			return this;
		}

		@Override
		public Span recordException(Throwable exception, Attributes additionalAttributes) {
			return this;
		}

		@Override
		public Span updateName(String name) {
			return this;
		}

		@Override
		public void end() {
			
		}

		@Override
		public void end(long timestamp, TimeUnit unit) {
		}

		@Override
		public SpanContext getSpanContext() {
			return SpanContext.getInvalid();
		}

		@Override
		public boolean isRecording() {
			return RecordTelemetryTest.recording;
		}
		
	}
	
	
}
