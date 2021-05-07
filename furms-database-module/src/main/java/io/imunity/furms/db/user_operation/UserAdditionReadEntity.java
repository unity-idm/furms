/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.db.sites.SiteEntity;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;

import java.util.Objects;
import java.util.UUID;

public class UserAdditionReadEntity extends UUIDIdentifiable {

	public final SiteEntity site;
	public final UUID projectId;
	public final UUID correlationId;
	public final String userId;
	public final int status;

	UserAdditionReadEntity(UUID id, SiteEntity site, UUID projectId, UUID correlationId, String userId, int status) {
		this.id = id;
		this.site = site;
		this.projectId = projectId;
		this.correlationId = correlationId;
		this.userId = userId;
		this.status = status;
	}

	public UserAddition toUserAddition(){
		return UserAddition.builder()
			.id(id.toString())
			.siteId(new SiteId(site.getId().toString(), new SiteExternalId(site.getExternalId())))
			.projectId(projectId.toString())
			.correlationId(new CorrelationId(correlationId.toString()))
			.userId(userId)
			.status(UserAdditionStatus.valueOf(status))
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAdditionReadEntity that = (UserAdditionReadEntity) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(site, that.site) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, site, correlationId, userId, status);
	}

	@Override
	public String toString() {
		return "UserAdditionReadEntity{" +
			"id=" + id +
			", site=" + site +
			", projectId=" + projectId +
			", correlationId=" + correlationId +
			", userId='" + userId + '\'' +
			", status=" + status +
			'}';
	}
}
