package com.smartstreamrdu.service.merging;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartstreamrdu.domain.DataAttribute;
import com.smartstreamrdu.domain.DataAttributeFactory;
import com.smartstreamrdu.domain.DataRow;
import com.smartstreamrdu.domain.DataType;
import com.smartstreamrdu.domain.DomainType;
import com.smartstreamrdu.service.normalized.NormalizedValueService;

import lombok.Setter;

/**
 * abstract class for SD and EN merge comparator
 */
@Component
public class AbstractNestedArrayComparator {

	
	@Autowired
	@Setter
	private NormalizedValueService normalizedService;


	/**
	 * Checks if all the values for all the key attributes equate.
	 * 
	 * @param feed
	 * @param dbRow
	 * @param arrayAttribute
	 * @param dataSourceValue
	 * @return
	 */
	protected boolean allKeysEquate(DataRow feed, DataRow dbRow, DataAttribute arrayAttribute,
			DomainType dataSourceValue) {
		List<DataAttribute> keyAttributes = DataAttributeFactory.getKeyAttributeForParentAttribute(arrayAttribute);
		return keyAttributes.stream()
				.allMatch(keyAttribute -> matchBasedOnKey(feed, dbRow, keyAttribute, dataSourceValue));
	}

	/**
	 * Checks and returns true if the value for the given keyAttribute is same in
	 * both the data rows.
	 * 
	 * @param feed
	 * @param dbRow
	 * @param keyAttribute
	 * @param dataSourceValue
	 * @return
	 */
	private boolean matchBasedOnKey(DataRow feed, DataRow dbRow, DataAttribute keyAttribute,
			DomainType dataSourceValue) {
		Serializable feedValue = getValueForAttribute(feed, keyAttribute, dataSourceValue);
		Serializable dbValue = getValueForAttribute(dbRow, keyAttribute, dataSourceValue);
		return feedValue != null && dbValue != null && feedValue.equals(dbValue);
	}

	/**
	 * Returns the value for the given key attribute from the given data row.
	 * 
	 * @param dataRow
	 * @param keyAttribute
	 * @return
	 */
	protected Serializable getValueForAttribute(DataRow dataRow, DataAttribute keyAttribute,
			DomainType dataSourceValue) {

		Serializable value = null;

		if (keyAttribute.getDataType().equals(DataType.DOMAIN)) {
			DomainType domainData = (DomainType) dataRow.getAttributeValue(keyAttribute).getValue();
			value = normalizedService.getNormalizedValueForDomainValue(keyAttribute, domainData,
					dataSourceValue.getVal());
		} else {
			value = dataRow.getAttributeValue(keyAttribute).getValue();
		}

		return value;
	}
}
