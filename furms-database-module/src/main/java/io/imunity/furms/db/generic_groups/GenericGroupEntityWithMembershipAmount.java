/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.util.Objects;
import java.util.UUID;

class GenericGroupEntityWithMembershipAmount extends UUIDIdentifiable {
	public final UUID communityId;
	public final String name;
	public final String description;
	public final int membershipAmount;

	GenericGroupEntityWithMembershipAmount(UUID id, UUID communityId, String name, String description, int membershipAmount) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.membershipAmount = membershipAmount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupEntityWithMembershipAmount that = (GenericGroupEntityWithMembershipAmount) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(membershipAmount, that.membershipAmount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityId, name, description, membershipAmount);
	}

	@Override
	public String toString() {
		return "GenericGroup{" +
			"communityId=" + communityId +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", assignmentAmount='" + membershipAmount + '\'' +
			", id=" + id +
			'}';
	}
}
