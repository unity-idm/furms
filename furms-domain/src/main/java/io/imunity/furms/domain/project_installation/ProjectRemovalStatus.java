/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.util.Arrays;

public enum ProjectRemovalStatus {
	PENDING(0), ACKNOWLEDGED(1), REMOVED(2), FAILED(3);

	ProjectRemovalStatus(int persistentId) {
		this.persistentId = persistentId;
	}

	private final int persistentId;

	public int getPersistentId() {
		return persistentId;
	}

	public static ProjectRemovalStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));	}
}
