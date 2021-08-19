/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus;

import java.util.Arrays;

public enum AcceptanceStatus {
	ACCEPTED(PolicyAcceptanceStatus.ACCEPTED), REJECTED(PolicyAcceptanceStatus.NOT_ACCEPTED), UNKNOWN(null);

	public final PolicyAcceptanceStatus policyAcceptanceStatus;

	AcceptanceStatus(PolicyAcceptanceStatus policyAcceptanceStatus) {
		this.policyAcceptanceStatus = policyAcceptanceStatus;
	}

	static AcceptanceStatus valeOf(PolicyAcceptanceStatus policyAcceptanceStatus){
		return Arrays.stream(values())
			.filter(status -> status.policyAcceptanceStatus.equals(policyAcceptanceStatus))
			.findAny()
			.orElse(UNKNOWN);
	}
}
