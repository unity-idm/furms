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
import io.imunity.furms.domain.user_operation.UserStatus;

import java.util.Objects;
import java.util.UUID;

public class UserAdditionReadEntity extends UUIDIdentifiable {

	public final SiteEntity site;
	public final UUID projectId;
	public final UUID correlationId;
	public final String userId;
	public final String uid;
	public final int status;

	UserAdditionReadEntity(UUID id, SiteEntity site, UUID projectId, UUID correlationId, String userId, String uid, int status) {
		this.uid = uid;
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
			.uid(uid)
			.status(UserStatus.valueOf(status))
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAdditionReadEntity that = (UserAdditionReadEntity) o;
		return status == that.status
				&& Objects.equals(site, that.site)
				&& Objects.equals(projectId, that.projectId)
				&& Objects.equals(correlationId, that.correlationId)
				&& Objects.equals(userId, that.userId)
				&& Objects.equals(uid, that.uid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(site, projectId, correlationId, userId, uid, status);
	}

	@Override
	public String toString() {
		return "UserAdditionReadEntity{" +
				"site=" + site +
				", projectId=" + projectId +
				", correlationId=" + correlationId +
				", userId='" + userId + '\'' +
				", uid='" + uid + '\'' +
				", status=" + status +
				'}';
	}
}
