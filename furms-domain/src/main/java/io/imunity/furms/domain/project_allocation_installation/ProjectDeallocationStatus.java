/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.util.Arrays;

public enum ProjectDeallocationStatus {
	PENDING(0), ACKNOWLEDGED(1), FAILED(2);

	ProjectDeallocationStatus(int persistentId) {
		this.persistentId = persistentId;
	}

	private final int persistentId;

	public int getPersistentId() {
		return persistentId;
	}

	public static ProjectDeallocationStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));
	}
}
