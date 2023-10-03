/*******************************************************************
 *
 * Copyright (c) 2009-2021 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	UdlMetricsDateComparator.java
 * Author:	Padgaonkar S
 * Date:	27-Oct-2021
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.den.enrichment.service;

import java.util.Comparator;
import java.util.Date;

import com.smartstreamrdu.persistence.domain.UdlMetrics;

/**
 * This class compare udlMetrics entries based on startDate
 * @author Padgaonkar
 *
 */
public class UdlMetricsDateComparator implements Comparator<UdlMetrics> {

	@Override
	public int compare(UdlMetrics m1, UdlMetrics m2) {

		Date initial = m1.getStartDate();
		Date after = m2.getStartDate();
		return initial.compareTo(after);
	}
}