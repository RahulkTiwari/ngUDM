/**
 * 
 */
package com.smartstreamrdu.service.jsonconvertor;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smartstreamrdu.commons.xrf.CrossRefBaseDocument;
import com.smartstreamrdu.service.jsonconverter.TimestampTypeAdapter;

/**
 * @author gehi
 *
 */
public class TimestampTypeAdapterTest {

	Gson gson = new GsonBuilder().disableHtmlEscaping().registerTypeAdapter(Timestamp.class, new TimestampTypeAdapter()).create();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testSerialize() {
		CrossRefBaseDocument baseDoc = new CrossRefBaseDocument();
		baseDoc.set_crossRefDocumentId("DocID1");
		baseDoc.set_securityId("Sec1");
		LocalDateTime lmd = LocalDateTime.of(2019, 01, 31, 10, 20, 30, 999000000);
		
		baseDoc.setLastModifiedDate(Timestamp.valueOf(lmd));
		
		String str = gson.toJson(baseDoc);
		assertEquals("", "{\"sdSecurityId\":\"Sec1\",\"crossRefDocumentId\":\"DocID1\",\"crossRefAttributes\":{},\"lastModifiedDate\":\"2019-01-31T10:20:30.999\"}", str);
	}

	@Test
	public final void testDeserialize() {
		CrossRefBaseDocument c = gson.fromJson("{\"sdSecurityId\":\"Sec1\",\"crossRefDocumentId\":\"DocID1\",\"crossRefAttributes\":{},\"lastModifiedDate\":\"2018-09-07T11:43:50.876\"}", CrossRefBaseDocument.class);
		LocalDateTime lmd = LocalDateTime.of(2018, 9, 7, 11, 43, 50, 876000000);
		assertEquals("Couldnt match date: " + c, Timestamp.valueOf(lmd), c.getLastModifiedDate());
	}

}
