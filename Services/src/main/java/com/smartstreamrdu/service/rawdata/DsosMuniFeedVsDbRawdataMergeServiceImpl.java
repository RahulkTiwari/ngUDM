package com.smartstreamrdu.service.rawdata;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

/**
 * @author Akshaykumar
 *
 * This class is specific for MUNI feed.
 * Here we are getting the feed object from incoming feed files & DB object 
 * from collection to match field & merge. 
 */
@Component("DsosMuniFeedVsDbRawdataMergeService")
public class DsosMuniFeedVsDbRawdataMergeServiceImpl extends AbstractDsosFeedVsDbRawdataMergeServiceImpl {
	
	protected static final String MUNI = "muni";

	@Override
	public String getArrayObjectKeyValue(JSONObject security) {
		String keyValue = null;
		if (security.containsKey(QUOTE)) {
			JSONObject quote = (JSONObject) security.get(QUOTE);
			keyValue = (String) quote.get(QUOTE_ID);
		}
		return keyValue;
	}

	@Override
	protected String getBaseKey() {
		return MUNI;
	}
}
