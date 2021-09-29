/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

class GenericGroupEntityWithAssignment extends UUIDIdentifiable {
	public final UUID communityId;
	public final String name;
	public final String description;
	public final UUID assignmentId;
	public final String userId;
	public final LocalDateTime memberSince;

	GenericGroupEntityWithAssignment(UUID id, UUID communityId, String name, String description, UUID assignmentId, String userId, LocalDateTime memberSince) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.assignmentId = assignmentId;
		this.userId = userId;
		this.memberSince = memberSince;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupEntityWithAssignment that = (GenericGroupEntityWithAssignment) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(assignmentId, that.assignmentId) &&
			Objects.equals(userId, that.userId) &&
			Objects.equals(memberSince, that.memberSince);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityId, name, description, assignmentId, userId, memberSince);
	}

	@Override
	public String toString() {
		return "GenericGroupEntityWithAssignment{" +
			"communityId=" + communityId +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", assignmentId=" + assignmentId +
			", userId='" + userId + '\'' +
			", memberSince=" + memberSince +
			", id=" + id +
			'}';
	}
}
