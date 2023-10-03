/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	JsonConverterTest.java
 * Author:	Divya Bharadwaj
 * Date:	14-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.domain;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.reflect.TypeToken;
import com.smartstreamrdu.commons.xrf.CrossRefBaseDocument;
import com.smartstreamrdu.commons.xrf.CrossRefChangeEntity;
import com.smartstreamrdu.commons.xrf.XrfAttributeValue;
import com.smartstreamrdu.commons.xrf.XrfMessage;
import com.smartstreamrdu.domain.CustomBigDecimal;
import com.smartstreamrdu.service.jsonconverter.JsonConverterUtil;


/**
 * @author Bharadwaj
 *
 */
public class JsonConverterTest {
	
	XrfMessage getMsgObject(){
		XrfMessage msg = new XrfMessage();
		List<CrossRefChangeEntity> crossRefChangeEntities=new ArrayList<>();
		msg.setCrossRefChangeEntities(crossRefChangeEntities);
		CrossRefChangeEntity entity = new CrossRefChangeEntity();
		crossRefChangeEntities.add(entity);
		CrossRefBaseDocument pojo=new CrossRefBaseDocument();
		entity.setPostChangeDocument(pojo);
		pojo.set_crossRefDocumentId("123");
		pojo.set_instrumentId("123");
		XrfAttributeValue ref=new XrfAttributeValue();
		ref.setName("isin");
		ref.setValue("ABCD");
		ref.setDataSourceId("123");
		pojo.addAttribute("isin",ref );
		return msg;
	}
	
	@Test
	public void convertToJson(){
		
		
		 XrfMessage msgObject = getMsgObject();
		String json=JsonConverterUtil.convertToJson(msgObject);
		System.out.println(json);
		XrfMessage obj =(XrfMessage) JsonConverterUtil.convertFromJson(json,XrfMessage.class);
		System.out.println(obj);
		System.out.println(msgObject);
		org.junit.Assert.assertEquals(obj, msgObject);
	}

	
	
	@Test
	public void convertCustomBigDecimal() throws ParseException{
		CustomBigDecimal obj=new CustomBigDecimal("5.001");
		Assert.assertNotNull(JsonConverterUtil.convertToJson(obj));
		String out="5.001";
		Assert.assertEquals(out,JsonConverterUtil.convertToJson(obj));
	}
	
	@Test
	public void convertLocalDate() throws ParseException{
		LocalDate obj=LocalDate.now();
		Assert.assertNotNull(JsonConverterUtil.convertToJson(obj));
		Assert.assertEquals("\""+obj.format(DateTimeFormatter.ISO_DATE)+"\"",JsonConverterUtil.convertToJson(obj));
	}
	
	@Test
	public void convertLocalDateTime() throws ParseException{
		LocalDateTime ldt = LocalDateTime.now();
		Timestamp obj = Timestamp.valueOf(ldt);
		Assert.assertNotNull(JsonConverterUtil.convertToJson(obj));
		Assert.assertEquals("\""+ldt.format(DateTimeFormatter.ISO_DATE_TIME)+"\"",JsonConverterUtil.convertToJson(obj));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void convertMap() {
		String mapString = "{\"testKeyLong\":100.00, \"testKeyString\":\"test string\", \"testKeyDouble\":100.10, \"testKeyBoolean\":true}";
		Map<String, Object> mapObject = JsonConverterUtil.convertFromJson(mapString, new TypeToken<Map<String,Object>>() {}.getType());
		Assert.assertEquals(100L, mapObject.get("testKeyLong"));
		Assert.assertEquals(100.10, mapObject.get("testKeyDouble"));
		Assert.assertEquals("test string", mapObject.get("testKeyString"));
		Assert.assertTrue((boolean) mapObject.get("testKeyBoolean"));
	}
}
