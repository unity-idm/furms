/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.util.Arrays;

public enum ProjectAllocationInstallationStatus {
	PROVISIONING_PROJECT(0, false),
	PENDING(1, false),
	ACKNOWLEDGED(2, false),
	INSTALLED(3, false),
	FAILED(4, true),
	PROJECT_INSTALLATION_FAILED(5, true);

	ProjectAllocationInstallationStatus(int persistentId, boolean failed) {
		this.persistentId = persistentId;
		this.failed = failed;
	}

	private final int persistentId;
	private final boolean failed;

	public int getPersistentId() {
		return persistentId;
	}

	public boolean isFailed() {
		return failed;
	}

	public static ProjectAllocationInstallationStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));	}
}
