/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.time.ZonedDateTime;

class ProtocolMessage {
	
	final String id;
	final MessageType type;
	final MessageStatus status;
	final ZonedDateTime sentOn;
	final ZonedDateTime ackOn;
	
	ProtocolMessage(String id, MessageType type, MessageStatus status,
			ZonedDateTime sentOn, ZonedDateTime ackOn) {
		this.id = id;
		this.type = type;
		this.status = status;
		this.sentOn = sentOn;
		this.ackOn = ackOn;
	}
}
