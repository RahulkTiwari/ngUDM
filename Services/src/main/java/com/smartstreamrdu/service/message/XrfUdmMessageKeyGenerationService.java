/**
 * 
 */
package com.smartstreamrdu.service.message;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.smartstreamrdu.commons.xrf.CrossRefBaseDocument;
import com.smartstreamrdu.commons.xrf.CrossRefChangeEntity;
import com.smartstreamrdu.commons.xrf.XrfMessage;
import com.smartstreamrdu.commons.xrf.XrfProcessMode;
import com.smartstreamrdu.domain.message.UdmMessageKey;

/**
 * @author ViKumar
 *
 */
@Component
public class XrfUdmMessageKeyGenerationService implements UdmMessageKeyGenerationService<XrfMessage> {

	@Override
	public UdmMessageKey generateUdmMessageKey(XrfMessage message) {

		Objects.requireNonNull(message, "xrfMessage can't be null");
		List<CrossRefChangeEntity> crossRefChangeEntities = message.getCrossRefChangeEntities();
		Objects.requireNonNull(crossRefChangeEntities, "crossRefChangeEntities can't be null");
		Optional<CrossRefChangeEntity> findFirst = crossRefChangeEntities.stream().findFirst();
		if (findFirst.isPresent()) {
			CrossRefBaseDocument crossRefBaseDoc = findFirst.get().getPostChangeDocument();
			return UdmMessageKey.builder().action(getAction(crossRefBaseDoc.getXrfProcessMode()))
					.variableAttributeValue(crossRefBaseDoc.getDocumentId()).build();
		}
		return null;
	}

	private String getAction(XrfProcessMode xrfProcessMode) {
		return xrfProcessMode == null ? XrfProcessMode.XRFDEFAULT.getXrfProcesModename()
				: xrfProcessMode.getXrfProcesModename();
	}

}
