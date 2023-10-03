/*******************************************************************
 *
 * Copyright (c) 2009-2018 The SmartStream Reference Data Utility 
 * All rights reserved. 
 *
 * File:	AbstractJavaBeanNonSerializableTest.java
 * Author:	Ravi Shah
 * Date:	20-Aug-2018
 *
 *******************************************************************
 */
package com.smartstreamrdu.service;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.meanbean.lang.Factory;
import org.meanbean.test.BeanTester;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import nl.jqno.equalsverifier.api.SingleTypeEqualsVerifierApi;

/**
 *
 */
@Ignore
@RunWith(Theories.class)
public abstract class AbstractJavaBeanNonSerializableTest<T> {

	public String[] getIgnoredFields(Object myBean) {
		return new String[0];
	}

	@Theory
	public void equalsAndHashCodeContract() {
		SingleTypeEqualsVerifierApi<T> verifier = EqualsVerifier.forClass(getBeanClass()).suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).withIgnoredFields(getIgnoredFields(getBeanClass()));
		
		if (prefabValues() != null) {
			AbstractJavaBeanNonSerializableTest<T>.Prafab<Object> prefabValues = prefabValues();
			verifier.withPrefabValues(prefabValues.clazz, prefabValues.obj1, prefabValues.obj2);
		}
		verifier.verify();
	}

	@Theory
	public void getterAndSetterCorrectness() throws Exception {
		final BeanTester beanTester = new BeanTester();
		Map<Class<?>, Factory<?>> settings = getFactorySettings();
		if (!settings.isEmpty()) {
			settings.forEach((k, v) -> beanTester.getFactoryCollection().addFactory(k, v));
		}
		beanTester.testBean(getBeanClass());
	}

	protected Map<Class<?>, Factory<?>> getFactorySettings() {
		return new HashMap<>();
	}

	protected abstract Class<T> getBeanClass();

	protected <E> Prafab<E> prefabValues() {
		return null;
	}

	public final class Prafab<E> {
		private final Class<E> clazz;

		private final E obj1;

		private final E obj2;

		/**
		 * @param clazz
		 * @param obj1
		 * @param obj2
		 */
		public Prafab(Class<E> clazz, E obj1, E obj2) {
			super();
			this.clazz = clazz;
			this.obj1 = obj1;
			this.obj2 = obj2;
		}

	}
}
