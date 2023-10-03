/**
 * 
 */
package com.smartstreamrdu.service.message;

import java.io.Serializable;

import com.smartstreamrdu.domain.message.UdmMessageKey;

/**
 * @author ViKumar
 * 
 * Service which helps to generate UdmMessageKey from Serializable message object.
 *
 */
public interface UdmMessageKeyGenerationService <T extends Serializable>{
	
	public UdmMessageKey generateUdmMessageKey(T message);

}
