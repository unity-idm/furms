/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.Arrays;

public enum UserRemovalStatus {
	PENDING(0), ACKNOWLEDGED(1), REMOVED(2), FAILED(3);

	UserRemovalStatus(int value) {
		this.value = value;
	}

	private final int value;

	public int getValue() {
		return value;
	}

	public static UserRemovalStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getValue() == status)
			.findAny()
			.orElse(null);
	}
}
