/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class UserAdditionJobId extends UUIDBasedIdentifier {

	public UserAdditionJobId(String id) {
		super(id);
	}

	public UserAdditionJobId(UUID id) {
		super(id);
	}

	public UserAdditionJobId(UserAdditionJobId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "UserAdditionJobId{" + "id=" + id + '}';
	}
}
