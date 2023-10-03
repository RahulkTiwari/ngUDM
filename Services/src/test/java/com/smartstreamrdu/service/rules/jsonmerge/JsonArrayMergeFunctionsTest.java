package com.smartstreamrdu.service.rules.jsonmerge;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class JsonArrayMergeFunctionsTest {
	
	JSONObject map1 = new JSONObject();
	JSONObject map2 = new JSONObject();
	JSONObject map3 = new JSONObject();
	JSONArray array1 = new JSONArray();
	JSONArray array2 = new JSONArray();
	JSONArray array3 = new JSONArray();

	private JsonArrayMergeFunctions getData() {
		JSONObject map = new JSONObject();
		JSONObject map1 = new JSONObject();
		JSONObject map2 = new JSONObject();
		JSONObject map3 = new JSONObject();
		JSONArray array = new JSONArray();

		map.put("_Value", "0.0024369");
		map.put("_date", "2015-12-29");
		array.add(map);

		map2.put("_Value", "0.1287332");
		map2.put("_date", "2042-12-29");
		array.add(map2);

		map1.put("rate", array);
		map3.put("step_schedule", map1);
		String[] path = { "_date" };
		String key = "step_schedule";
		JsonArrayMergeFunctions data = JsonArrayMergeFunctions.builder().jsonArray(array).postMergePrefixKey(key).mergingAttributePaths(path).build();

		return data;
	}

	@Before
	public void setup() {
		map1.put("_start_date", "2015-12-29");
		map1.put("contingent_interest_ind", "false");
		array1.add(map1);
		map3.put("_start_date", "2043-12-29");
		map3.put("_end_date", "2042-03-15");
		array1.add(map3);
		map2.put("coupon_payment_feature", array1);
		array2.add(map1);
		array3.add(map1);
	}
	
	
	@Test
	public void test_MergeJsonArray() {
		String[] arr1 = { "_start_date" };
		String[] arr2 = { "_start_date" };

		JsonArrayMergeFunctions mergeService = getData();
		JsonArrayMergeFunctions output = mergeService.merge(array1, arr1, "coupon_payment_feature");
		JsonArrayMergeFunctions output2 = output.merge(array1, arr2, "test_tag");
		JsonArrayMergeFunctions output3 = output2.merge(array2, arr2, "test_tag");
		JsonArrayMergeFunctions output4 = output3.merge(array3, arr2, "test_date");
		Assert.assertNotNull(output4);
		Assert.assertTrue(output4.getMergingAttributePaths().length == 4);
	}
	
	@Test
	public void test_initiateArrayMerge() {
		JSONObject map = new JSONObject();
		JSONObject map1 = new JSONObject();
		JSONObject map2 = new JSONObject();
		JSONObject map3 = new JSONObject();
		JSONArray array = new JSONArray();

		map.put("_Value", "0.0024369");
		map.put("_date", "2015-12-29");
		array.add(map);

		map2.put("_Value", "0.1287332");
		map2.put("_date", "2042-12-29");
		array.add(map2);

		map1.put("rate", array);
		map3.put("step_schedule", map1);
		String[] path = { "_date" };
		String key = "step_schedule";
		
		JsonArrayMergeFunctions initialObject = JsonArrayMergeFunctions.builder().build().initiateArrayMerge(array, path, key);
		Assert.assertNotNull(initialObject);
		Assert.assertTrue(path.equals(initialObject.getMergingAttributePaths()));
	}
	
	@Test
	public void test_convertToJsonArray() {

		map1.put("_Value", "0.0024369");
		map1.put("_date", "2015-12-29");

		map2.put("_Value", "0.1287332");
		map2.put("_date", "2042-12-29");

		ArrayList<JSONObject> list = new ArrayList<JSONObject>();

		list.add(map1);
		list.add(map2);
		String[] path = { "_date" };
		String key = "step_schedule";

		JsonArrayMergeFunctions initialObject = JsonArrayMergeFunctions.builder().build().initiateArrayMerge(list, path,
				key);
		Assert.assertNotNull(initialObject);
		Assert.assertTrue(path.equals(initialObject.getMergingAttributePaths()));

		JsonArrayMergeFunctions initialObject1 = JsonArrayMergeFunctions.builder().build().initiateArrayMerge(null,
				path, key);
		Assert.assertNotNull(initialObject1.getJsonArray());

		JsonArrayMergeFunctions initialObject2 = JsonArrayMergeFunctions.builder().build().initiateArrayMerge(map1,
				path, key);
		Assert.assertNotNull(initialObject2.getJsonArray());
	}

}
