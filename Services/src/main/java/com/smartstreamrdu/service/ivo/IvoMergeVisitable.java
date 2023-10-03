/**
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved.
 * 
 * File: IvoMergeVisitable.java
 * Author : SaJadhav
 * Date : 13-Feb-2019
 * 
 */
package com.smartstreamrdu.service.ivo;

import com.smartstreamrdu.service.visitor.Visitable;
import com.smartstreamrdu.service.visitor.Visitor;

/**
 * @author SaJadhav
 *
 */
public class IvoMergeVisitable implements Visitable<IvoMergeVisitable> {
	
	private IvoContainer ivoContainer;
	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.visitor.Visitable#accept(com.smartstreamrdu.service.visitor.Visitor)
	 */
	@Override
	public void accept(Visitor<IvoMergeVisitable> visitor) throws Exception {
		visitor.visit(this);
	}
	/**
	 * @return the ivoContainer
	 */
	public IvoContainer getIvoContainer() {
		return ivoContainer;
	}
	/**
	 * @param ivoContainer the ivoContainer to set
	 */
	public void setIvoContainer(IvoContainer ivoContainer) {
		this.ivoContainer = ivoContainer;
	}

}
