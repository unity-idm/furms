/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("alarm_user")
class AlarmUserEntity extends UUIDIdentifiable {
	public final String userId;

	AlarmUserEntity(String userId) {
		this.id = UUID.randomUUID();
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlarmUserEntity that = (AlarmUserEntity) o;
		return Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}

	@Override
	public String toString() {
		return "AlarmUserEntity{" +
			", userId='" + userId + '\'' +
			'}';
	}
}
