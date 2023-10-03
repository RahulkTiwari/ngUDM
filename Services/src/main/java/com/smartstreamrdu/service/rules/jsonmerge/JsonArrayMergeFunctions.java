/*******************************************************************
 *
 * Copyright (c) 2009-2022 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	JsonArrayMergeFunctions.java
 * Author:	Rushikesh Dedhia
 * Date:	24-May-2022
 *
 *******************************************************************/
package com.smartstreamrdu.service.rules.jsonmerge;

import static com.jayway.jsonpath.JsonPath.using;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jayway.jsonpath.Configuration;
import com.smartstreamrdu.domain.UdmJwayDefaults;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * @author Dedhia
 *
 */
@Data
@Slf4j
@Builder
public class JsonArrayMergeFunctions implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2759507409943920947L;
	
	private static final Configuration CONF = Configuration.builder().options().build();
	private static final String DOT = ".";

	/**
	 *  Array of strings which are to be used as key attributes to compare JSONArrays
	 */
	private String[] mergingAttributePaths;

	/**
	 *  String key to be used as a prefix when two objects of JSONArrays are merged.
	 */
	private String postMergePrefixKey;

	/**
	 *  The JSON array.
	 */
	private JSONArray jsonArray;
	
	static {
		initializeJayWayDefaults();
	}
	
	/**
	 * Initialize the jayway CachepProvider to use  {com.smartstreamrdu.domain.UdmJwayCache}
	 * instead of Default LRU cache
	 */
	private static void initializeJayWayDefaults() {
		UdmJwayDefaults.initialize();
	}
	
	
	
	/**
	 *  This method initiates the process of merging 2 JSON arrays together
	 *  by creating an initial object of type JsonArrayMergeServiceFunctions.
	 *  
	 *  This is done by using the builder methods for JsonArrayMergeServiceFunctions
	 *  class.
	 *  
	 * @param input
	 * @param mergeAttribute
	 * @param resultKey
	 * @return
	 */
	public JsonArrayMergeFunctions initiateArrayMerge(Serializable input, String[] mergeAttribute, String resultKey) {
		return (JsonArrayMergeFunctions.builder().jsonArray(getInputAsArray(input))
					.mergingAttributePaths(mergeAttribute).postMergePrefixKey(resultKey).build());
	}

	/**
	 *  This method merges the JSONArray provided in the parameters with the JSON object held
	 *  in the current object. This merge is based on the mergeAttributes of both the JSONArrays.
	 *  
	 *  The matching objects of both the arrays are combined together into a larger JSONObject. The 
	 *  resultKey is the key for those objects in the merged object. This merged object is later added to the
	 *  resultant JSONArray.
	 *  
	 *  For objects from the JSONArrays that do not match, they are added independently as JSONObjects and are
	 *  later added to the resultant JSONArray.
	 * 
	 * @param input
	 * @param mergeAttributes
	 * @param resultKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JsonArrayMergeFunctions merge(Serializable input, String[] mergeAttributes, String resultKey) {
		JSONArray resultArray = new JSONArray();
		JSONArray mergedArray = this.jsonArray;
		String prefixKey = this.postMergePrefixKey;
		
		// Convert input data to JSONArray format.
		JSONArray inputArray = getInputAsArray(input);
		
		Iterator<Object> mergedArrayIterator = mergedArray.iterator();

		while (mergedArrayIterator.hasNext()) {
			JSONObject resultObject = new JSONObject();
			Object oldObject = mergedArrayIterator.next();
			Object attributeValue1 = getAttributeValue(this.mergingAttributePaths, oldObject);

			Iterator<Object> inputIterator = inputArray.iterator();
			while (inputIterator.hasNext()) {
				Object inputObject = inputIterator.next();
				Object attributeValue2 = getAttributeValue(mergeAttributes, inputObject);

				if (null != attributeValue1 && null != attributeValue2
						&& attributeValue1.equals(attributeValue2)) {
					if (doesPrefixAlreadyExists(this.getMergingAttributePaths(), prefixKey)) {
						// If the prefix already exists, then we need to add only the new input object to the 
						// merged object.
						addAllObjectsFromOldJSONObjectToResult(resultObject, (HashMap<String, Object>) oldObject);
						resultObject.putIfAbsent(resultKey, inputObject);
						resultArray.add(resultObject);
					} else {
						resultObject.putIfAbsent(prefixKey, oldObject);
						resultObject.putIfAbsent(resultKey, inputObject);
						resultArray.add(resultObject);
					}
					// Remove elements that have matched from both the arrays to reduce iterations.
					mergedArrayIterator.remove();
					inputIterator.remove();
					break;
				}
			}
		}

		// Add the remainder of elements from the input array to the final result array.
		addElementsFromArrayToResult(inputArray, resultKey, resultArray);
		// Add the remainder of elements from the existing json array to the final result array.
		addElementsFromArrayToResult(mergedArray, prefixKey, resultArray);
		// Consolidate all the old and new mergeAttributes together.
		Set<String> finalSet = getFinalSetOfKeysFromBothArrays(mergeAttributes, resultKey, mergingAttributePaths, postMergePrefixKey);
 		
		return (JsonArrayMergeFunctions.builder().jsonArray(resultArray)
				.mergingAttributePaths((String[]) finalSet.toArray(new String[finalSet.size()])).postMergePrefixKey(resultKey).build());
	}

	/**
	 *  Checks the input provided and converts it to JSONArray type if required.
	 *  It is to accept input either of type JSONArray or JSONObject.
	 *  
	 *  For input of type other than these two, it throws a @IllegalArgumentException
	 *  exception.
	 * 
	 * @param input
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JSONArray getInputAsArray(Serializable input) {
		if (input == null) {
			return new JSONArray();
		} else if (input instanceof JSONArray) {
			return (JSONArray) input;
		} else if (input instanceof JSONObject || input instanceof org.json.simple.JSONObject) {
			JSONArray inputArray = new JSONArray();
			inputArray.add(input);
			return inputArray;
		} else if (input instanceof List) {
			return createJsonArrayFromInput((List<Serializable>) input);
		}
		else {
			log.debug("Invalid input provided for JSONArray merge. Expected input types are JSONArray and JSONObject. Provided input : {} was of type {}", input, input.getClass());
			throw new IllegalArgumentException("Input provided is invalid. Expected input either of type JSONArray of JSONObject. Supplied input was : "+input);
		}
	}

	/**
	 *  Iterates over the input which is in a form of list and adds all its 
	 *  elements to the new JSONArray and returns the same.
	 * 
	 * @param input
	 * @return
	 */
	private JSONArray createJsonArrayFromInput(List<Serializable> input) {
		JSONArray inputArray = new JSONArray();
		input.forEach(inputArray::add);
		return inputArray;
	}

	/**
	 *  Checks if the old object has value for the provided key.
	 *  
	 *  If available and is the only entry, it will just add that one entry to the resultObject.
	 *  
	 *  If not, it will add all the entries from the old object to the result object.
	 * 
	 * @param key
	 * @param resultObject
	 * @param ((HashMap)oldObject)
	 */
	private void addAllObjectsFromOldJSONObjectToResult( JSONObject resultObject, HashMap<String, Object> oldObject) {
		oldObject.entrySet().forEach(dataObject ->
			resultObject.put(dataObject.getKey(), dataObject.getValue())
		);
	}

	/** 
	 *  Checks if the key is already a part of the existing mergeArributePaths.
	 * @param mergeAttributePaths
	 * @param key
	 * @return
	 */
	private boolean doesPrefixAlreadyExists(String[] mergeAttributePaths, String key) {
		for (String compareKey : mergeAttributePaths) {
			if (compareKey.startsWith(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *  This method creates a set of keys from both the old and the new
	 *  arrays.
	 *  
	 *  First it iterates over the array of attributes for the new key. It prefixes the 
	 *  values with the newArrayPrefixKey so that it creates a JSON readable entry for the 
	 *  next iteration.
	 *  
	 *  Then it iterates over the array of attributes for the old array and adds them to 
	 *  the final set of values.
	 * 
	 * @param newArrayAttributes
	 * @param newArrayPrefixKey
	 * @param oldArrayMergeAttributes
	 * @param oldArrayPrefixKey
	 * @return
	 */
	private Set<String> getFinalSetOfKeysFromBothArrays(String[] newArrayAttributes, String newArrayPrefixKey,
			String[] oldArrayMergeAttributes, String oldArrayPrefixKey) {

		Set<String> finalSet = new HashSet<>();

		// Carry forward new mergeAttributes.
		for (String value : newArrayAttributes) {
			// Prefix the values with the newArrayPrefixKey so that it creates a JSON readable entry for the 
			//  next iteration.
			finalSet.add(newArrayPrefixKey + DOT + value);
		}
		
		// Carry forward old merge attributes. 
		for (String value : oldArrayMergeAttributes) {
			if (StringUtils.contains(value, DOT)) {
				// If the value is already prefixed, we simply need to add it to the final set.
				finalSet.add(value);
			} else {
				// There will be a case in the first iteration where we will only have the key attribute names
				// taken as they are when we initiate the merge process.
				// For that case we will need to prefix the value with the prefix key so that it can later be
				// used in the merge if required in the future.
				finalSet.add(oldArrayPrefixKey + DOT + value);
			}
		}
		return finalSet;
	}

	/**
	 *  Returns the value from the @param oldObject using the keys
	 *  provided in @param mergeAttributePaths.
	 *  
	 *  The @param mergeAttributePaths contain multiple keys. This method returns the 
	 *  value from the @param oldObject for the first key for which a non null value is available
	 *  in the @param oldObject.
	 * 
	 * @param mergeAttributePaths
	 * @param oldObject
	 * @return
	 */
	private Object getAttributeValue(String[] mergeAttributePaths, Object oldObject) {
		for (String mergeAttributePath : mergeAttributePaths) {
			try {
				Object value = using(CONF).parse(oldObject).read(mergeAttributePath);
				if (null != value) {
					// If the value is available for the given key, return the value immediately.
					return value;
				}
			} catch (Exception exception) {
				log.debug("Following error occured while fetching value from JSONObject.", exception);
			}
		}
		// If there is no value available for any of the keys, we return null.
		return null;
	}

	/**
	 * Adds the elements from the @param inputArray to the
	 * 
	 * @param resultArray using the String resultKey.
	 * 
	 * @param inputArray
	 * @param resultKey
	 * @param resultArray
	 */
	private void addElementsFromArrayToResult(JSONArray inputArray, String resultKey, JSONArray resultArray) {
		Iterator<Object> inputIterator = inputArray.iterator();
		inputIterator.forEachRemaining(dataObject -> {
			if (doesPrefixAlreadyExists(this.mergingAttributePaths, resultKey)) {
				resultArray.add(dataObject);
			} else {
				JSONObject object = new JSONObject();
				object.put(resultKey, dataObject);
				resultArray.add(object);
			}
		});
	}

}
