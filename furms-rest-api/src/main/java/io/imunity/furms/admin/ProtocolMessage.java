/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.time.Instant;

class ProtocolMessage {
	final String id;

	final MessageType type;

	final MessageStatus status;

	final Instant sent;

	final Instant lastResponse;

	ProtocolMessage(String id, MessageType type, MessageStatus status, Instant sent,
			Instant lastResponse) {
		this.id = id;
		this.type = type;
		this.status = status;
		this.sent = sent;
		this.lastResponse = lastResponse;
	}
}
