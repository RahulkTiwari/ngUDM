/*******************************************************************
 *
 * Copyright (c) 2009-2017 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	TypeAdapter.java
 * Author:	Divya Bharadwaj
 * Date:	15-May-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service.jsonconverter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.smartstreamrdu.domain.CustomBigDecimal;

/**
 * @author Bharadwaj
 * @param <T>
 *
 */
public class SerializableTypeAdapterFactory implements TypeAdapterFactory   {

	/* (non-Javadoc)
	 * @see com.google.gson.TypeAdapterFactory#create(com.google.gson.Gson, com.google.gson.reflect.TypeToken)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		/**
		 * Earlier if get any garbage value for boolean attribute, 
		 * then system was adding a default value 'false' to that attribute.
		 * Because of that we were unable to acknowledge user if any garbage value comes as input.
		 * To solve this, we create a new TypeAdapter for boolean, that will throw exception in this case. 
		 */
		if (Boolean.class.equals(type.getRawType())) {
			return (TypeAdapter<T>) new BooleanTypeAdapter();
		}else if (Serializable.class.equals(type.getRawType())) {
			return (TypeAdapter<T>) gson.getAdapter(Object.class);
		} else if (CustomBigDecimal.class.equals(type.getRawType())) {
			return (TypeAdapter<T>) new CustomBigDecimalTypeAdapter();
		} else if (LocalDate.class.equals(type.getRawType())) {
			return (TypeAdapter<T>) new LocalDateTypeAdapter();
		} else if (LocalDateTime.class.equals(type.getRawType())) {
			return (TypeAdapter<T>) new LocalDateTimeTypeAdapter();
		}
		return null;
	}

	
}
