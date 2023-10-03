/**
 * 
 */
package com.smartstreamrdu.service.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.smartstreamrdu.domain.Record;

/**
 * @author Ashok Thanage
 *
 */
public class LoaderExceptionLoggingHandlerImplTest {

	private LoaderExceptionLoggingHandlerImpl  exceptionloggingHandler = new LoaderExceptionLoggingHandlerImpl();
	
	private Record record1 = new Record();
	
	private Exception e = null;
	
	/**
	 * Test method for {@link com.smartstreamrdu.service.exception.LoaderExceptionLoggingHandlerImpl#logException(java.lang.Exception, java.lang.String, java.io.Serializable)}.
	 */
	@Test
	public void testLogException() {
		exceptionloggingHandler.logException(e, this.getClass().getSimpleName(), record1);
		assertEquals(false, record1.isToBeProcessed());
	}

}
