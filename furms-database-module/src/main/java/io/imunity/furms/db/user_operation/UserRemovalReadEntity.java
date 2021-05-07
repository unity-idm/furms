/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserRemoval;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;

import java.util.Objects;
import java.util.UUID;

public class UserRemovalReadEntity extends UUIDIdentifiable {

	public final SiteId siteId;
	public final UUID projectId;
	public final UUID correlationId;
	public final UUID userAdditionalId;
	public final String userId;
	public final int status;

	UserRemovalReadEntity(UUID id, SiteId siteId, UUID projectId, UUID correlationId, UUID userAdditionalId, String userId, int status) {
		this.id = id;
		this.siteId = siteId;
		this.projectId = projectId;
		this.correlationId = correlationId;
		this.userAdditionalId = userAdditionalId;
		this.userId = userId;
		this.status = status;
	}

	public UserRemoval toUserRemoval(){
		return UserRemoval.builder()
			.id(id.toString())
			.siteId(siteId)
			.projectId(projectId.toString())
			.correlationId(new CorrelationId(correlationId.toString()))
			.userId(userId)
			.status(UserRemovalStatus.valueOf(status))
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRemovalReadEntity that = (UserRemovalReadEntity) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(userAdditionalId, that.userAdditionalId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, siteId, correlationId, userAdditionalId, userId, status);
	}

	@Override
	public String toString() {
		return "UserRemovalReadEntity{" +
			"id=" + id +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", correlationId=" + correlationId +
			", userAdditionalId=" + userAdditionalId +
			", userId='" + userId + '\'' +
			", status=" + status +
			'}';
	}
}
