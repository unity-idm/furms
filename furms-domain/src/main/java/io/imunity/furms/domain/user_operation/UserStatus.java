/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.Arrays;

public enum UserStatus {
	ADDING_PENDING(0, true, false),
	ADDING_ACKNOWLEDGED(1, true,false),
	ADDED(2, true,false),
	ADDING_FAILED(3, true,true),
	REMOVAL_PENDING(4, true,false),
	REMOVAL_ACKNOWLEDGED(5, true,false),
	REMOVED(6, true,false),
	REMOVAL_FAILED(7, true,true);

	UserStatus(int persistentId, boolean terminal, boolean errorStatus) {
		this.persistentId = persistentId;
		this.terminal = terminal;
		this.errorStatus = errorStatus;
	}

	private final int persistentId;
	private final boolean terminal;
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
