/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.time.ZonedDateTime;

class PolicyAcceptance {

	enum AcceptanceStatus {
		ACCEPTED, REJECTED, UNKNOWN
	}
	
	final String fenixUserId;
	final String policyId;
	final AcceptanceStatus accepted;
	final ZonedDateTime processedOn;
	final int revision;

	PolicyAcceptance(String policyId, int revision, String fenixUserId, AcceptanceStatus accepted,
			ZonedDateTime processedOn) {
		this.policyId = policyId;
		this.revision = revision;
		this.fenixUserId = fenixUserId;
		this.accepted = accepted;
		this.processedOn = processedOn;
	}
}
