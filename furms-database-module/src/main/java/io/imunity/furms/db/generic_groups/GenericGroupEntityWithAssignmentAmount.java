/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.util.Objects;
import java.util.UUID;

class GenericGroupEntityWithAssignmentAmount extends UUIDIdentifiable {
	public final UUID communityId;
	public final String name;
	public final String description;
	public final int assignmentAmount;

	GenericGroupEntityWithAssignmentAmount(UUID id, UUID communityId, String name, String description, int assignmentAmount) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.assignmentAmount = assignmentAmount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupEntityWithAssignmentAmount that = (GenericGroupEntityWithAssignmentAmount) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(assignmentAmount, that.assignmentAmount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityId, name, description, assignmentAmount);
	}

	@Override
	public String toString() {
		return "GenericGroup{" +
			"communityId=" + communityId +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", assignmentAmount='" + assignmentAmount + '\'' +
			", id=" + id +
			'}';
	}
}
