/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoAggregationHandler.java
 * Author : SaJadhav
 * Date : 13-Mar-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;

/**
 * @author SaJadhav
 *
 */
public interface IvoAggregationHandler {

	/**
	 *  It merges the value of Attribute from sdIvoContainer with sdContainer.
	 *  It should be used for in-memory merging of SdIvoContainer to sdContainer
	 *  <PRE>
	 *  e.g.
	 *  
	 *  CUSIP : {FEED :"123"} //value in sdContainer
	 *  
	 *  CUSIP : {RDU :"345"} //value in sdIvoContainer
	 *  
	 *  // value in sdContainer after merge
	 *  CUSIP :{
	 *  		FEED:"123", 
	 *  		RDU :"345"
	 *  		}
	 *  </PRE>
	 * @param sdContainer
	 * @param sdIvoContainer
	 * @param ivoDataAttribute
	 */
	void handleAttributeMerge(DataContainer sdContainer, DataContainer sdIvoContainer, DataAttribute ivoDataAttribute);
	
	/**
	 *  It merges the value of Attribute from sdDataRow with sdIvoDataRow.
	 *  It is used for merging Nested Attributes
	 *  It should be used for in-memory merging of SdIvoContainer to sdContainer
	 *  <PRE>
	 *  e.g.
	 *  //value in sdContainer
	 *  instrumentLegalEntityRelation :{
	 *    relationType:"Issuer",
	 *    refData: {
	 *    		"nameLong":{{FEED:"IBM LTD"},
	 *    		"legalEntityId":{FEED:"797922"}
	 *    		}
	 *    }
	 *  }
	 *  
	 *  //value in sdIvoContainer
	 *  instrumentLegalEntityRelation :{
	 *    relationType:"Issuer",
	 *    refData: {
	 *    		"nameLong":{{RDU:"IBM Limited"},
	 *    		"legalEntityId":{RDU:"797929"}
	 *    		}
	 *    	}
	 *  }
	 *  
	 *  // value in sdContainer after merge
	 *  
	 *  instrumentLegalEntityRelation :{
	 *    relationType:"Issuer",
	 *    refData: {
	 *    		"nameLong":{
	 *    					   FEED:"IBM LTD",
	 *    					   RDU: "IBM Limited"
	 *    					},
	 *    		"legalEntityId":{
	 *    						  FEED:"797922",
	 *    						  RDU:"797929" 
	 *    					}
	 *   		 }
	 *    		}
	 *  }
	 *  </PRE>
	 * @param sdDataRow
	 * @param sdIvoDataRow
	 * @param ivoDataAttribute
	 * @param ivoParentDataAttribute
	 */
	void handleAttributeMerge(DataRow sdDataRow, DataRow sdIvoDataRow, DataAttribute ivoDataAttribute, DataAttribute ivoParentDataAttribute);
}
