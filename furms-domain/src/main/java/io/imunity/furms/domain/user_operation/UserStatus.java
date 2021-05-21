/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.Arrays;

public enum UserStatus {
	ADDING_PENDING(0),
	ADDING_ACKNOWLEDGED(1),
	ADDED(2),
	ADDING_FAILED(3),
	REMOVAL_PENDING(4),
	REMOVAL_ACKNOWLEDGED(5),
	REMOVED(6),
	REMOVAL_FAILED(7);

	UserStatus(int persistentId) {
		this.persistentId = persistentId;
	}

	private final int persistentId;
	private static final TransitionValidator TRANSITION_VALIDATOR = new TransitionValidator();

	public int getPersistentId() {
		return persistentId;
	}

	public boolean isTransitionalTo(UserStatus userStatus) {
		return TRANSITION_VALIDATOR.isTransitional(this, userStatus);
	}

	public static UserStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Bad status code - %s, it shouldn't happen", status)));
	}

	public boolean isErrorStatus() {
		return this == ADDING_FAILED || this == REMOVAL_FAILED;
	}
}
