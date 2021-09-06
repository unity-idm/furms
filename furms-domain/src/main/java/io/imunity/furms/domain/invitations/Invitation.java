/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FenixUserId;

import java.time.LocalDateTime;
import java.util.Objects;

public class Invitation {
	public final InvitationId id;
	public final ResourceId resourceId;
	public final String resourceName;
	public final String originator;
	public final FenixUserId userId;
	public final String email;
	public final Role role;
	public final InvitationCode code;
	public final LocalDateTime utcExpiredAt;

	Invitation(InvitationId id, ResourceId resourceId, String resourceName, String originator,  FenixUserId userId,
	           String email, Role role, InvitationCode code, LocalDateTime utcExpiredAt) {
		this.id = id;
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.originator = originator;
		this.userId = userId;
		this.email = email;
		this.role = role;
		this.code = code;
		this.utcExpiredAt = utcExpiredAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Invitation that = (Invitation) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(resourceId, that.resourceId) &&
			Objects.equals(resourceName, that.resourceName) &&
			Objects.equals(originator, that.originator) &&
			Objects.equals(userId, that.userId) &&
			role == that.role &&
			Objects.equals(code, that.code) &&
			Objects.equals(utcExpiredAt, that.utcExpiredAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, resourceId, resourceName, originator, userId, email, role, code, utcExpiredAt);
	}

	@Override
	public String toString() {
		return "Invitation{" +
			"id=" + id +
			", resourceId=" + resourceId +
			", resourceName=" + resourceName +
			", originator=" + originator +
			", userId='" + userId + '\'' +
			", email='" + email + '\'' +
			", role=" + role +
			", unityCode='" + code + '\'' +
			", expiredAt=" + utcExpiredAt +
			'}';
	}

	public static InvitationBuilder builder() {
		return new InvitationBuilder();
	}

	public static final class InvitationBuilder {
		public InvitationId id;
		public ResourceId resourceId;
		public String resourceName;
		public String originator;
		public FenixUserId userId = FenixUserId.empty();
		public String email;
		public Role role;
		public InvitationCode code;
		public LocalDateTime utcExpiredAt;

		private InvitationBuilder() {
		}

		public InvitationBuilder id(InvitationId id) {
			this.id = id;
			return this;
		}

		public InvitationBuilder resourceId(ResourceId resourceId) {
			this.resourceId = resourceId;
			return this;
		}

		public InvitationBuilder userId(FenixUserId userId) {
			this.userId = userId;
			return this;
		}

		public InvitationBuilder email(String email) {
			this.email = email;
			return this;
		}

		public InvitationBuilder role(Role role) {
			this.role = role;
			return this;
		}

		public InvitationBuilder resourceName(String resourceName) {
			this.resourceName = resourceName;
			return this;
		}

		public InvitationBuilder originator(String originator) {
			this.originator = originator;
			return this;
		}

		public InvitationBuilder code(String code) {
			this.code = new InvitationCode(code);
			return this;
		}

		public InvitationBuilder code(InvitationCode code) {
			this.code = code;
			return this;
		}

		public InvitationBuilder utcExpiredAt(LocalDateTime expiredAt) {
			this.utcExpiredAt = expiredAt;
			return this;
		}

		public Invitation build() {
			return new Invitation(id, resourceId, resourceName, originator, userId, email, role, code, utcExpiredAt);
		}
	}
}
