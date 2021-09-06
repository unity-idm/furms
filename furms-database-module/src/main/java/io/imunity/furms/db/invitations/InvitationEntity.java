/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.invitations;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FenixUserId;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("invitation")
class InvitationEntity extends UUIDIdentifiable {

	public final UUID resourceId;
	public final String userId;
	public final String email;
	public final String resourceName;
	public final String originator;
	public final int resourceType;
	public final String roleAttribute;
	public final String roleValue;
	public final String code;
	public final LocalDateTime expiredAt;

	InvitationEntity(UUID id, UUID resourceId, String resourceName, String originator, String userId, String email, int resourceType, String roleAttribute, String roleValue, String code, LocalDateTime expiredAt) {
		this.id = id;
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.originator = originator;
		this.userId = userId;
		this.email = email;
		this.resourceType = resourceType;
		this.roleAttribute = roleAttribute;
		this.roleValue = roleValue;
		this.code = code;
		this.expiredAt = expiredAt;
	}

	Invitation toInvitation(){
		return Invitation.builder()
			.id(new InvitationId(id))
			.resourceId(new ResourceId(resourceId, ResourceType.valueOf(resourceType)))
			.resourceName(resourceName)
			.originator(originator)
			.userId(new FenixUserId(userId))
			.email(email)
			.role(Role.translateRole(roleAttribute, roleValue)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Bad role attribute - %s or role value - %s, it shouldn't happen", roleAttribute, roleValue)))
			)
			.code(code)
			.utcExpiredAt(expiredAt)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InvitationEntity that = (InvitationEntity) o;
		return resourceType == that.resourceType &&
			Objects.equals(id, that.id) &&
			Objects.equals(resourceId, that.resourceId) &&
			Objects.equals(resourceName, that.resourceName) &&
			Objects.equals(originator, that.originator) &&
			Objects.equals(userId, that.userId) &&
			Objects.equals(email, that.email) &&
			Objects.equals(roleAttribute, that.roleAttribute) &&
			Objects.equals(roleValue, that.roleValue) &&
			Objects.equals(code, that.code) &&
			Objects.equals(expiredAt, that.expiredAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, resourceId, resourceName, originator, userId, email, resourceType, roleAttribute, roleValue, code, expiredAt);
	}

	@Override
	public String toString() {
		return "InvitationEntity{" +
			"id=" + id +
			", resourceId=" + resourceId +
			", resourceName=" + resourceName +
			", originator=" + originator +
			", userId='" + userId + '\'' +
			", email='" + email + '\'' +
			", resourceType=" + resourceType +
			", roleAttribute='" + roleAttribute + '\'' +
			", roleValue='" + roleValue + '\'' +
			", code='" + code + '\'' +
			", expiredAt=" + expiredAt +
			'}';
	}

	public static InvitationEntityBuilder builder() {
		return new InvitationEntityBuilder();
	}

	public static final class InvitationEntityBuilder {
		private UUID id;
		private UUID resourceId;
		private String resourceName;
		private String originator;
		private String userId;
		private String email;
		private int resourceType;
		private String roleAttribute;
		private String roleValue;
		private String code;
		private LocalDateTime expiredAt;

		private InvitationEntityBuilder() {
		}

		public InvitationEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public InvitationEntityBuilder resourceId(UUID resourceId) {
			this.resourceId = resourceId;
			return this;
		}

		public InvitationEntityBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public InvitationEntityBuilder email(String email) {
			this.email = email;
			return this;
		}

		public InvitationEntityBuilder resourceName(String resourceName) {
			this.resourceName = resourceName;
			return this;
		}

		public InvitationEntityBuilder originator(String originator) {
			this.originator = originator;
			return this;
		}

		public InvitationEntityBuilder resourceType(int resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public InvitationEntityBuilder resourceType(ResourceType resourceType) {
			this.resourceType = resourceType.getPersistentId();
			return this;
		}

		public InvitationEntityBuilder roleAttribute(String roleAttribute) {
			this.roleAttribute = roleAttribute;
			return this;
		}

		public InvitationEntityBuilder roleValue(String roleValue) {
			this.roleValue = roleValue;
			return this;
		}

		public InvitationEntityBuilder role(Role role) {
			this.roleAttribute = role.unityRoleAttribute;
			this.roleValue = role.unityRoleValue;
			return this;
		}

		public InvitationEntityBuilder code(String code) {
			this.code = code;
			return this;
		}

		public InvitationEntityBuilder expiredAt(LocalDateTime expiredAt) {
			this.expiredAt = expiredAt;
			return this;
		}

		public InvitationEntity build() {
			return new InvitationEntity(id, resourceId, resourceName, originator, userId, email, resourceType, roleAttribute, roleValue, code, expiredAt);
		}
	}
}
