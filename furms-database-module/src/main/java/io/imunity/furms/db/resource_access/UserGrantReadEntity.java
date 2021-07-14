/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.util.Objects;
import java.util.UUID;

class UserGrantReadEntity extends UUIDIdentifiable {
	public final UUID siteId;
	public final String siteExternalId;
	public final UUID projectId;
	public final UUID projectAllocationId;
	public final String userId;

	UserGrantReadEntity(UUID id, UUID siteId, String siteExternalId, UUID projectId, UUID projectAllocationId, String userId) {
		this.id = id;
		this.siteId = siteId;
		this.siteExternalId = siteExternalId;
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserGrantReadEntity that = (UserGrantReadEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(siteExternalId, that.siteExternalId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, siteExternalId, projectId, projectAllocationId, userId);
	}

	@Override
	public String toString() {
		return "UserAllocationEntity{" +
			"id=" + id +
			", siteId=" + siteId +
			", externalSiteId=" + siteExternalId +
			", projectId=" + projectId +
			", projectAllocationId=" + projectAllocationId +
			", userId='" + userId + '\'' +
			'}';
	}
}
