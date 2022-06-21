/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.util.Arrays;

public enum ProjectAllocationInstallationStatus {
	PROVISIONING_PROJECT(0,  false, false),
	PENDING(1,  false, false),
	ACKNOWLEDGED(2,  false,false),
	FAILED(3, true,true),
	PROJECT_INSTALLATION_FAILED(4, true,true),
	UPDATING(5, false,false),
	UPDATING_FAILED(6, true,true),
	INSTALLED(7, true,false);

	ProjectAllocationInstallationStatus(int persistentId, boolean terminal, boolean failed) {
		this.persistentId = persistentId;
		this.terminal = terminal;
		this.failed = failed;
	}

	private final int persistentId;
	private final boolean terminal;
	private final boolean failed;

	public int getPersistentId() {
		return persistentId;
	}

	public boolean isFailed() {
		return failed;
	}

	public boolean isTerminal() {
		return terminal;
	}

	public static ProjectAllocationInstallationStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));	}
}
