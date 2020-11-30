/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class PolicyAcceptance {
	final String fenixUserId;

	final String policyId;

	final boolean accepted;

	PolicyAcceptance(String policyId, String fenixUserId, boolean accepted) {
		this.policyId = policyId;
		this.fenixUserId = fenixUserId;
		this.accepted = accepted;
	}

}
