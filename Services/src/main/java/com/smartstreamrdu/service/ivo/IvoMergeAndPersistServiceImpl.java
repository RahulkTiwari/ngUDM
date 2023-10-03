/**
 * Copyright (c) 2009-2019 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoMergeAndPersistServiceImpl.java
 * Author : SaJadhav
 * Date : 13-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.exception.UdmTechnicalException;

/**
 * @author SaJadhav
 *
 */
@Component
public class IvoMergeAndPersistServiceImpl implements IvoMergeAndPersistService{
	
	@Autowired
	private List<IvoMergeAndPersistVisitor> ivoMergeVisitors;
	

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.persist.IvoMergeAndPersistService#mergeAndPersist(com.smartstreamrdu.persistence.service.IvoContainer)
	 */
	@Override
	public void mergeAndPersist(IvoContainer ivoContainer) throws UdmTechnicalException {
		final IvoMergeVisitable mergeVisitable = new IvoMergeVisitable();

		mergeVisitable.setIvoContainer(ivoContainer);
		// mergeVisitable.set
		for (IvoMergeAndPersistVisitor ivoMergeVisitor : ivoMergeVisitors) {
			try {
				mergeVisitable.accept(ivoMergeVisitor);
			} catch (Exception e) {
				throw new UdmTechnicalException("Exception in mergeAndPersist of ivoContainer having document id:"
						+ ivoContainer.getSdDocumentId(), e);
			}
		}
	}

}
