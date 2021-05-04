/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.util.Arrays;

public enum ProjectDeallocationStatus {
	PENDING(0), ACKNOWLEDGED(1), FAILED(2);

	ProjectDeallocationStatus(int value) {
		this.value = value;
	}

	private final int value;

	public int getValue() {
		return value;
	}

	public static ProjectDeallocationStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getValue() == status)
			.findAny()
			.orElse(null);
	}
}
