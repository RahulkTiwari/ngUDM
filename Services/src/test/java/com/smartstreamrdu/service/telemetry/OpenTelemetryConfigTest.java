/**
 * 
 */
package com.smartstreamrdu.service.telemetry;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.persistence.mongodb.MongoConfig;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;

/**
 * @author Akshay Gehi
 *
 */
@ActiveProfiles({"jaeger", "junit"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class OpenTelemetryConfigTest {

	@Autowired
	Tracer tracer;

	/**
	 * Test method for
	 * {@link com.smartstreamrdu.service.telemetry.OpenTelemetryConfig#tracer(io.opentelemetry.sdk.trace.SdkTracerProvider)}.
	 */
	@Test
	public void testTracer() {
		Class<? extends Tracer> cls = tracer.getClass();
		assertFalse("NoOp Tracer should not be used", TracerProvider.noop().getClass().isAssignableFrom(cls));
	}

}
