/**
 * 
 */
package com.smartstreamrdu.service.inactive;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.LockLevel;
import com.smartstreamrdu.exception.UdmBaseException;

/**
 * @author ViKumar
 *
 */
public interface InstrumentInactivationService {

	/**
	 * Generic inactivation of sdData instrument which
	 * 
	 * takes @sdDataContainer and marks them INACTIVE  with Lock level @inactivationLockLevel and which component is initiating it @inactivationInitiatedBy 
	 * 
	 * And calls the service MergeAndPersistService to persist into sdData and trigger the events and sends message to other components
	 * 
	 * @param sdDataContainerIterator
	 * @throws UdmBaseException
	 */
	public void inactivateInstruments(DataContainer sdDataContainer, LockLevel inactivationLockLevel,String inactivationInitiatedBy,String dataSource) throws UdmBaseException;

}
