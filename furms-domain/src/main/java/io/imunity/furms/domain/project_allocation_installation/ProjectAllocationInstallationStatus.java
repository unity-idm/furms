/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.util.Arrays;

public enum ProjectAllocationInstallationStatus {
	PROVISIONING_PROJECT(0, false, false),
	PENDING(1, true, false),
	ACKNOWLEDGED(2,  true,false),
	INSTALLED(3, false,false),
	FAILED(4, false,true),
	PROJECT_INSTALLATION_FAILED(5, false,true);

	ProjectAllocationInstallationStatus(int persistentId, boolean installing, boolean failed) {
		this.persistentId = persistentId;
		this.installing = installing;
		this.failed = failed;
	}

	private final int persistentId;
	private final boolean failed;
	private final boolean installing;

	public int getPersistentId() {
		return persistentId;
	}

	public boolean isFailed() {
		return failed;
	}

	public boolean isInstalling() {
		return installing;
	}

	public static ProjectAllocationInstallationStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));	}
}
