/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	JsonConverterImpl.java
 * Author:	Divya Bharadwaj
 * Date:	15-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonconverter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;

/**
 * @author Bharadwaj
 *
 */
public class JsonConverterUtil {

	private static final Logger _logger = LoggerFactory.getLogger(JsonConverterUtil.class);

	private JsonConverterUtil() {
	}

	private static Gson gson = getGson();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.jsonconverter.JsonConverter#convertToJson(java.io.
	 * Serializable)
	 */
	public static String convertToJson(Object obj) {
		if (obj instanceof Map) {
			return gson.toJson(obj, Map.class);
		}
		return gson.toJson(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.smartstreamrdu.service.jsonconverter.JsonConverter#convertFromJson(com.
	 * google.gson.Gson, java.lang.Class)
	 */
	public static <T extends Serializable> T convertFromJson(String json, Class<T> destClass) {
		return gson.fromJson(json, destClass);
	}

	/**
	 * @param transformJson
	 * @param type
	 * @return
	 */
	public static <T extends Serializable> T convertFromJson(String transformJson, Type type) {
		return gson.fromJson(transformJson, type);
	}

	/**
	 * @return
	 */
	@SuppressWarnings("serial")
	protected static Gson getGson() {
		return new GsonBuilder().registerTypeAdapterFactory(new SerializableTypeAdapterFactory()).disableHtmlEscaping()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonDeserializer())
				.registerTypeAdapter(new TypeToken<Map<String,Object>>() {}.getType(), new MapDeserializerDoubleAsInt())
				.registerTypeAdapter(Timestamp.class, new TimestampTypeAdapter()).create();
	}

	/*
	 * -------------------------------------------------------- 
	 * Class Adapters
	 * --------------------------------------------------------
	 */
	/**
	 * LocalDateTimeJsonDeserializer creates LocalDateTime from json objects
	 */
	private static final class LocalDateTimeJsonDeserializer implements JsonDeserializer<LocalDateTime> {
		@Override
		public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString());
		}
	}
	
	private static final class MapDeserializerDoubleAsInt implements JsonDeserializer<Map<String, Object>> {

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			return (Map<String, Object>) read(json);
		}
		
		private Object read(JsonElement in) {

			if (in.isJsonArray()) {
				List<Object> list = new ArrayList<>();
				JsonArray arr = in.getAsJsonArray();
				for (JsonElement anArr : arr) {
					list.add(read(anArr));
				}
				return list;
			} else if (in.isJsonObject()) {
				Map<String, Object> map = new LinkedTreeMap<>();
				JsonObject obj = in.getAsJsonObject();
				Set<Map.Entry<String, JsonElement>> entitySet = obj.entrySet();
				for (Map.Entry<String, JsonElement> entry : entitySet) {
					map.put(entry.getKey(), read(entry.getValue()));
				}
				return map;
			} else if (in.isJsonPrimitive()) {
				JsonPrimitive prim = in.getAsJsonPrimitive();
				if (prim.isBoolean()) {
					return prim.getAsBoolean();
				} else if (prim.isString()) {
					return prim.getAsString();
				} else if (prim.isNumber()) {

					Number num = prim.getAsNumber();
					// here you can handle double int/long values
					// and return any type you want
					// this solution will transform 3.0 float to long values
					if (Math.ceil(num.doubleValue()) == num.longValue()) {
						return num.longValue();
					} else {
						return num.doubleValue();
					}
				}
			}
			return null;
		}
	}

	/**
	 * Creates an instance of org.json.simple.JSONObject from jsonString
	 * @param jsonString
	 * @return
	 */
	public static JSONObject convertToSimpleJson(String jsonString) {
		try {
			JSONParser parser = new JSONParser();
			return (JSONObject) parser.parse(jsonString);
		} catch (ParseException e) {
			_logger.error("Error while convering the string to JSONObject :" + jsonString, e);
		}
		return null;
	}
}
