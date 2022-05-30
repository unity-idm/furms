/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import io.imunity.furms.domain.communities.CommunityId;

import java.util.Objects;

public class ProjectGroup {
	private final ProjectId id;
	private final String name;
	private final CommunityId communityId;

	public ProjectGroup(ProjectId id, String name, CommunityId communityId) {
		this.id = id;
		this.name = name;
		this.communityId = communityId;
	}

	public ProjectId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public CommunityId getCommunityId() {
		return communityId;
	}

	public static ProjectGroupBuilder builder() {
		return new ProjectGroupBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectGroup that = (ProjectGroup) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(name, that.name) &&
			Objects.equals(communityId, that.communityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, communityId);
	}

	@Override
	public String toString() {
		return "ProjectGroup{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", communityId=" + communityId +
			'}';
	}

	public static class ProjectGroupBuilder {
		private ProjectId id;
		private String name;
		private CommunityId communityId;

		public ProjectGroupBuilder id(ProjectId id) {
			this.id = id;
			return this;
		}

		public ProjectGroupBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectGroupBuilder communityId(CommunityId communityId) {
			this.communityId = communityId;
			return this;
		}

		public ProjectGroup build() {
			return new ProjectGroup(id, name, communityId);
		}
	}
}
