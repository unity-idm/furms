/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import io.imunity.furms.domain.users.FenixUserId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class GenericGroupMembership {
	public final GenericGroupId genericGroupId;
	public final FenixUserId fenixUserId;
	public final LocalDateTime utcMemberSince;

	GenericGroupMembership(GenericGroupId genericGroupId, FenixUserId fenixUserId, LocalDateTime utcMemberSince) {
		this.genericGroupId = genericGroupId;
		this.fenixUserId = fenixUserId;
		this.utcMemberSince = utcMemberSince;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupMembership that = (GenericGroupMembership) o;
		return Objects.equals(genericGroupId, that.genericGroupId) &&
			Objects.equals(fenixUserId, that.fenixUserId) &&
			Objects.equals(utcMemberSince, that.utcMemberSince);
	}

	@Override
	public int hashCode() {
		return Objects.hash(genericGroupId, fenixUserId, utcMemberSince);
	}

	@Override
	public String toString() {
		return "GenericGroupAssignment{" +
			", genericGroupId=" + genericGroupId +
			", fenixUserId=" + fenixUserId +
			", utcMemberSince=" + utcMemberSince +
			'}';
	}

	public static GenericGroupAssignmentBuilder builder() {
		return new GenericGroupAssignmentBuilder();
	}

	public static final class GenericGroupAssignmentBuilder {
		private GenericGroupId genericGroupId;
		private FenixUserId fenixUserId;
		private LocalDateTime utcMemberSince;

		private GenericGroupAssignmentBuilder() {
		}

		public GenericGroupAssignmentBuilder genericGroupId(GenericGroupId genericGroupId) {
			this.genericGroupId = genericGroupId;
			return this;
		}

		public GenericGroupAssignmentBuilder genericGroupId(UUID genericGroupId) {
			this.genericGroupId = new GenericGroupId(genericGroupId);
			return this;
		}

		public GenericGroupAssignmentBuilder fenixUserId(FenixUserId fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public GenericGroupAssignmentBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = new FenixUserId(fenixUserId);
			return this;
		}

		public GenericGroupAssignmentBuilder utcMemberSince(LocalDateTime utcMemberSince) {
			this.utcMemberSince = utcMemberSince;
			return this;
		}

		public GenericGroupMembership build() {
			return new GenericGroupMembership(genericGroupId, fenixUserId, utcMemberSince);
		}
	}
}
