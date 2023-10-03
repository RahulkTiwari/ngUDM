/*******************************************************************
 *
 * Copyright (c) 2009-2020 The SmartStream Reference Data Utility
 * All rights reserved. 
 *
 * File:    MoodysRatingRankingService.java
 * Author:  Dedhia
 * Date:    21-Sep-2020
 *
 ********************************************************************/
package com.smartstreamrdu.service.proforma;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import com.smartstreamrdu.domain.proforma.MoodysRatingRankings;
import com.smartstreamrdu.persistence.cache.CacheService;

/**
 *  This class serves as a cache interface for moodysRatingRankings collection.
 * 
 * @author Dedhia
 *
 */
public interface MoodysRatingRankingCacheService {
	
	/**
	 *  This method returns all the rankings available in the moodysRatingRankings collection.
	 * 
	 * @return
	 */
	@Cacheable(sync=true, cacheManager = CacheService.CACHE_MANAGER, value = CacheService.CACHE_MOODYS_RATING_RANKINGS)
	List<MoodysRatingRankings> getAllMoodysRatingsRankings();

}
