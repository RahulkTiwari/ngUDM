package com.smartstreamrdu.service.rawdata;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

/**
 * @author Akshaykumar
 * 
 * This class is specific to GOVCORP feed. 
 * Here we are getting the feed object from incoming feed files & DB object 
 * from collection to match field & merge.
 */
@Component("DsosGovCorpFeedVsDbRawdataMergeService")
public class DsosGovcorpFeedVsDbRawdataMergeServiceImpl extends AbstractDsosFeedVsDbRawdataMergeServiceImpl {
	
	protected static final String SEDOL = "sedol";
	private static final String GOVCORP = "govcorp";
	protected static final String MIC_OPOL = "mic_opol";

	@Override
	public String getArrayObjectKeyValue(JSONObject security) {
		String keyValue = null;
		if (security.containsKey(QUOTE)) {
			JSONObject quote = (JSONObject) security.get(QUOTE);
			keyValue = (String) quote.get(QUOTE_ID);
		} else if (security.containsKey(MIC_OPOL)) {
			JSONObject micOpol = (JSONObject) security.get(MIC_OPOL);
			keyValue = (String) micOpol.get(SEDOL);
		}
		return keyValue;
	}

	@Override
	protected String getBaseKey() {
		return GOVCORP;
	}
}
