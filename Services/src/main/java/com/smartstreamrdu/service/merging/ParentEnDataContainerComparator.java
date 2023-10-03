/**
 * 
 */
package com.smartstreamrdu.service.merging;

import java.util.List;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * ENS Parent Comparator
 * @author RKaithwas
 *
 */
@Component("ENParentComparator")
public class ParentEnDataContainerComparator extends SdDataContainerComparator implements ParentContainerComparator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 379965279821283488L;

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.ParentContainerComparator#compareDataContainer(com.smartstreamrdu.domain.DataContainer, java.util.List)
	 */
	@Override
	public DataContainer compareDataContainer(DataContainer feedDContainer, List<DataContainer> dbContainers) throws UdmTechnicalException {
		
		return super.compareContainersAndReturn(null,feedDContainer, dbContainers);

	}

}
