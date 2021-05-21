/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.Arrays;

public enum UserStatus {
	ADDING_PENDING(0, false),
	ADDING_ACKNOWLEDGED(1, false),
	ADDED(2, false),
	ADDING_FAILED(3, true),
	REMOVAL_PENDING(4, false),
	REMOVAL_ACKNOWLEDGED(5, false),
	REMOVED(6, false),
	REMOVAL_FAILED(7, true);

	UserStatus(int persistentId, boolean errorStatus) {
		this.persistentId = persistentId;
		this.errorStatus = errorStatus;
	}

	private final int persistentId;
	private final boolean errorStatus;
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
		return errorStatus;
	}
}
