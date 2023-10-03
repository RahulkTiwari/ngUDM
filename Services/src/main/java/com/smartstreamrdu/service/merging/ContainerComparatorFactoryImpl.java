/*******************************************************************
 *
 * All rights reserved. 
 *
 * File: ParentContainerComparatorFactoryImpl.java
 * Author: Rushikesh Dedhia
 * Date: Jun 1, 2018
 *
 *******************************************************************/
package com.smartstreamrdu.service.merging;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataContainer;
import com.smartstreamrdu.domain.DataLevel;

/**
 * @author Dedhia
 *
 */
@Component
public class ContainerComparatorFactoryImpl implements ContainerComparatorFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6310432339945980621L;

	@Autowired
	private Map<String, ParentContainerComparator> parentComparator;

	@Autowired
	private Map<String, ChildContainerComparator> childComparator;
	
	@Autowired
	private Map<String,NestedArrayComparator> nestedArrayComparator;
	
	private static final String EXCEPTION_MESSAGE = "The data container recieved was null.";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.merging.ParentContainerComparatorFactory#
	 * getParentContainerComparator(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public ParentContainerComparator getParentContainerComparator(DataContainer dataContainer) {

		if (dataContainer == null) {
			throw new IllegalArgumentException(EXCEPTION_MESSAGE);
		}

		String type = getType(dataContainer);

		if (type != null) {
			return parentComparator.get(type + "ParentComparator");
		}
		return null;
	}

	/**
	 * @param dataContainer
	 * @return
	 */
	private String getType(DataContainer dataContainer) {
		DataLevel level = dataContainer.getLevel();
		String type = null;
		switch (level) {
		case INS:
		case SEC:
		case LE:
			type="SD";
			break;
		case EN:
			type="EN";
			break;
		case XRF_INS:
		case XRF_SEC:
			type = "XRF";
			break;
		case Document:
		case IVO_DOC:
		case PROFORMA_DOC:
			type=null;
			break;
		case SD_RAW_DATA:
		case EN_RAW_DATA:
			type = "RAW";
			break;
		case IVO_INS:
		case IVO_SEC:
			type="IVO";
			break;
		case PROFORMA_INS:
			type="Proforma";
			break;
		default:
			type = "STATIC";
			break;
		}
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smartstreamrdu.service.merging.ContainerComparatorFactory#
	 * getChildContainerComparator(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public ChildContainerComparator getChildContainerComparator(DataContainer dataContainer) {
		if (dataContainer == null) {
			throw new IllegalArgumentException(EXCEPTION_MESSAGE);
		}

		String type = getType(dataContainer);

		if (type != null) {
			return childComparator.get(type + "ChildComparator");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.smartstreamrdu.service.merging.ContainerComparatorFactory#getNestedArrayComparator(com.smartstreamrdu.domain.DataContainer)
	 */
	@Override
	public NestedArrayComparator getNestedArrayComparator(DataContainer dataContainer) {
		if (dataContainer == null) {
			throw new IllegalArgumentException(EXCEPTION_MESSAGE);
		}

		String type = getType(dataContainer);

		if (type != null) {
			return nestedArrayComparator.get(type + "NestedComparator");
		}
		return null;
	}

}
