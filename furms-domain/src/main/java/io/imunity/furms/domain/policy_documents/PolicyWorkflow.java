/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Arrays;

public enum PolicyWorkflow {
	PAPER_BASED(0), WEB_BASED(1);

	private final int persistentId;

	PolicyWorkflow(int persistentId) {
		this.persistentId = persistentId;
	}

	public int getPersistentId() {
		return persistentId;
	}

	public static PolicyWorkflow valueOf(int status){
		return Arrays.stream(values())
			.filter(policyWorkflow -> policyWorkflow.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));
	}
}
