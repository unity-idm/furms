/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz.roles;

import io.imunity.furms.domain.Id;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

public class ResourceId {
	public final Id id;
	public final ResourceType type;

	public ResourceId(Id id, ResourceType type) {
		this.id = id;
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceId that = (ResourceId) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override
	public String toString() {
		return "ResourceId{" +
			"id='" + id + '\'' +
			", type='" + type + '\'' +
			'}';
	}

	public ProjectId asProjectId() {
		try {
			return (ProjectId) id;
		} catch (ClassCastException e) {
			throw new IdClassCastException(String.format("%s cannot be cast to %s", id.getClass().getName(),
				ProjectId.class.getName()) , e);
		}
	}

	public CommunityId asCommunityId() {
		try {
			return (CommunityId) id;
		} catch (ClassCastException e) {
			throw new IdClassCastException(String.format("%s cannot be cast to %s", id.getClass().getName(),
				CommunityId.class.getName()) , e);
		}
	}

	public SiteId asSiteId() {
		try {
			return (SiteId) id;
		} catch (ClassCastException e) {
			throw new IdClassCastException(String.format("%s cannot be cast to %s", id.getClass().getName(),
				SiteId.class.getName()) , e);
		}
	}
}
