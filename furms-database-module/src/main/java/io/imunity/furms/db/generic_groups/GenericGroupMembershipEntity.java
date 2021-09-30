/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("generic_group_membership")
class GenericGroupMembershipEntity extends UUIDIdentifiable {
	public final UUID genericGroupId;
	public final String userId;
	public final LocalDateTime memberSince;

	GenericGroupMembershipEntity(UUID id, UUID genericGroupId, String userId, LocalDateTime memberSince) {
		this.id = id;
		this.genericGroupId = genericGroupId;
		this.userId = userId;
		this.memberSince = memberSince;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupMembershipEntity that = (GenericGroupMembershipEntity) o;
		return Objects.equals(genericGroupId, that.genericGroupId) && Objects.equals(userId, that.userId) && Objects.equals(memberSince, that.memberSince);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, genericGroupId, userId, memberSince);
	}

	@Override
	public String toString() {
		return "GenericGroupAssignmentEntity{" +
			"genericGroupId=" + genericGroupId +
			", userId='" + userId + '\'' +
			", memberSince=" + memberSince +
			", id=" + id +
			'}';
	}

	public static GenericGroupAssignmentEntityBuilder builder() {
		return new GenericGroupAssignmentEntityBuilder();
	}

	public static final class GenericGroupAssignmentEntityBuilder {
		private UUID id;
		private UUID genericGroupId;
		private String userId;
		private LocalDateTime memberSince;

		private GenericGroupAssignmentEntityBuilder() {
		}

		public GenericGroupAssignmentEntityBuilder genericGroupId(UUID genericGroupId) {
			this.genericGroupId = genericGroupId;
			return this;
		}

		public GenericGroupAssignmentEntityBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public GenericGroupAssignmentEntityBuilder memberSince(LocalDateTime memberSince) {
			this.memberSince = memberSince;
			return this;
		}

		public GenericGroupAssignmentEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public GenericGroupMembershipEntity build() {
			return new GenericGroupMembershipEntity(id, genericGroupId, userId, memberSince);
		}
	}
}
