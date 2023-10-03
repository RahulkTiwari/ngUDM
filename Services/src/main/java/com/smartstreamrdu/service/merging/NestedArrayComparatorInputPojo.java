/**
 * 
 */
package com.smartstreamrdu.service.merging;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataRowIterator;

import lombok.Builder;
import lombok.Data;

/**
 * @author Dedhia
 *
 */
@Data
@Builder
public class NestedArrayComparatorInputPojo {
	
	private DataAttribute arrayAttribute;
	
	private DataRow feed; 
	
	private DataRowIterator dbIterator;
	
	private DataContainer feedDataContainer;
	
	private DataContainer dbDataContainer;
}
