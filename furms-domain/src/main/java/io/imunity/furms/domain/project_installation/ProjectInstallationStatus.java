/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.util.Arrays;

public enum ProjectInstallationStatus {
	PENDING(0, false),
	ACKNOWLEDGED(1, false),
	INSTALLED(2, true),
	FAILED(3, true);


	ProjectInstallationStatus(int persistentId, boolean terminal) {
		this.persistentId = persistentId;
		this.terminal = terminal;
	}

	private final int persistentId;
	private final boolean terminal;

	public int getPersistentId() {
		return persistentId;
	}

	public boolean isTerminal() {
		return terminal;
	}

	public static ProjectInstallationStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));	}
}
