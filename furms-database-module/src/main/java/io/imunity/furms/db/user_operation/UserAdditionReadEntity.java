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
	public final String  uid;
	public final UserAdditionStatus status;

	UserAdditionReadEntity(UUID id, SiteEntity site, UUID projectId, UUID correlationId, String userId, String uid, UserAdditionStatus status) {
		this.id = id;
		this.site = site;
		this.projectId = projectId;
		this.correlationId = correlationId;
		this.userId = userId;
		this.uid = uid;
		this.status = status;
	}

	public UserAddition toUserAddition(){
		return UserAddition.builder()
			.id(id.toString())
			.siteId(new SiteId(site.getId().toString(), new SiteExternalId(site.getExternalId())))
			.projectId(projectId.toString())
			.correlationId(new CorrelationId(correlationId.toString()))
			.userId(userId)
			.uid(uid)
			.status(status)
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
			Objects.equals(userId, that.userId) &&
			Objects.equals(uid, that.uid) && status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, site, correlationId, userId, uid, status);
	}

	@Override
	public String toString() {
		return "UserAdditionReadEntity{" +
			"id=" + id +
			", site=" + site +
			", projectId=" + projectId +
			", correlationId=" + correlationId +
			", userId='" + userId + '\'' +
			", uid='" + uid + '\'' +
			", status=" + status +
			'}';
	}
}
