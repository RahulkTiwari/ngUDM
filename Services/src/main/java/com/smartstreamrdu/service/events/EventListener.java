/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File: ChangeEventListener.java
 * Author: Rushikesh Dedhia
 * Date: May 11, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.events;

import java.io.Serializable;

import com.smartstreamrdu.service.listener.ListenerEvent;

/**
 * @author Dedhia
 *
 */
public interface EventListener<E extends Serializable> {
	
	/**
	 *  This method will be propagate the change in the data container to the respective component.
	 *  Every implementation of this interface will have a  component specific implementation of this method.
	 * @param changeEventPojo
	 */
	public void propogateEvent(E changeEventPojo);
	
	/**
	 * Determines whether the event is applicable for the listener
	 * @param event
	 * @return
	 */
	boolean isEventApplicable(ListenerEvent event);
	
	/**
	 * Create the input from the object arguments
	 * @param args
	 */
	E createInput(ChangeEventListenerInputCreationContext inputCreationContext);

}
