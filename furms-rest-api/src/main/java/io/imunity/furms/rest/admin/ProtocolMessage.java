/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.time.ZonedDateTime;

class ProtocolMessage {
	
	final String id;
	final String jsonContent;
	final MessageStatus status;
	final ZonedDateTime sentOn;
	final ZonedDateTime ackOn;
	final int retryCount;

	ProtocolMessage(String id, String jsonContent, MessageStatus status,
	                ZonedDateTime sentOn, ZonedDateTime ackOn, int retryCount) {
		this.id = id;
		this.jsonContent = jsonContent;
		this.status = status;
		this.sentOn = sentOn;
		this.ackOn = ackOn;
		this.retryCount = retryCount;
	}
}
