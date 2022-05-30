/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UserAdditionId {
	public final UUID id;

	public UserAdditionId(UUID id) {
		this.id = id;
	}

	public UserAdditionId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public UserAdditionId(UserAdditionId id) {
		this.id = Optional.ofNullable(id)
			.map(resourceCreditId -> resourceCreditId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAdditionId userAdditionId = (UserAdditionId) o;
		return Objects.equals(id, userAdditionId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "UserAdditionId{" +
			"id=" + id +
			'}';
	}
}
