package com.smartstreamrdu.service.proforma;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smartstreamrdu.domain.proforma.MoodysRatingRankings;
import com.smartstreamrdu.junit.framework.AbstractEmbeddedMongodbJunitParent;
import com.smartstreamrdu.persistence.mongodb.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoConfig.class })
public class MoodysRatingRankingCacheServiceImpTest extends AbstractEmbeddedMongodbJunitParent {
	
	@Autowired
	private MoodysRatingRankingCacheService service;

	@Test
	public void testGetAllMoodysRatingsRankings() {
		List<MoodysRatingRankings> rankings= service.getAllMoodysRatingsRankings();
		assertNotNull(rankings);
		assertEquals(false, rankings.isEmpty());
	}

}
