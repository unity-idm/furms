/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_site_access;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("user_site_access")
public class UserSiteAccessEntity extends UUIDIdentifiable {

	public final UUID projectId;
	public final UUID siteId;
	public final String userId;

	UserSiteAccessEntity(UUID projectId, UUID siteId, String userId) {
		this.projectId = projectId;
		this.siteId = siteId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSiteAccessEntity that = (UserSiteAccessEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, siteId, userId);
	}

	@Override
	public String toString() {
		return "UserSiteAccessEntity{" +
			"id=" + id +
			", projectId=" + projectId +
			", siteId=" + siteId +
			", userId='" + userId + '\'' +
			'}';
	}
}
