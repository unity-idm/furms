/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

class GenericGroupEntityWithMembership extends UUIDIdentifiable {
	public final UUID communityId;
	public final String name;
	public final String description;
	public final String userId;
	public final LocalDateTime memberSince;

	GenericGroupEntityWithMembership(UUID id, UUID communityId, String name, String description, String userId, LocalDateTime memberSince) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.userId = userId;
		this.memberSince = memberSince;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupEntityWithMembership that = (GenericGroupEntityWithMembership) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(userId, that.userId) &&
			Objects.equals(memberSince, that.memberSince);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityId, name, description, userId, memberSince);
	}

	@Override
	public String toString() {
		return "GenericGroupEntityWithAssignment{" +
			"communityId=" + communityId +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", userId='" + userId + '\'' +
			", memberSince=" + memberSince +
			", id=" + id +
			'}';
	}
}
