/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.Arrays;

public enum UserAdditionStatus {
	PENDING(0), ACKNOWLEDGED(1), ADDED(2), FAILED(3);

	UserAdditionStatus(int persistentId) {
		this.persistentId = persistentId;
	}

	private final int persistentId;

	public int getPersistentId() {
		return persistentId;
	}

	public static UserAdditionStatus valueOf(int status){
		return Arrays.stream(values())
			.filter(userRemovalStatus -> userRemovalStatus.getPersistentId() == status)
			.findAny()
			.orElse(null);
	}
}
