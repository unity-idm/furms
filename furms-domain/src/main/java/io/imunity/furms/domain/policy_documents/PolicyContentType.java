/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Arrays;

public enum PolicyContentType {
	EMBEDDED(0), PDF(1);

	private final int persistentId;

	PolicyContentType(int persistentId) {
		this.persistentId = persistentId;
	}

	public int getPersistentId() {
		return persistentId;
	}

	public static PolicyContentType valueOf(int status){
		return Arrays.stream(values())
			.filter(policyContentType -> policyContentType.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));
	}
}
