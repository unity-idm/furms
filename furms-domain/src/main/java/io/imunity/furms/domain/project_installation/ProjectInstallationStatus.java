/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.util.Arrays;
import java.util.Set;

public enum ProjectInstallationStatus {
	PENDING(0), ACKNOWLEDGED(1), INSTALLED(2), FAILED(3);

	public static final Set<ProjectInstallationStatus> NOT_TERMINAL_STATES = Set.of(PENDING, ACKNOWLEDGED);

	ProjectInstallationStatus(int persistentId) {
		this.persistentId = persistentId;
	}

	private final int persistentId;

	public int getPersistentId() {
		return persistentId;
	}

	public static ProjectInstallationStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));	}
}
