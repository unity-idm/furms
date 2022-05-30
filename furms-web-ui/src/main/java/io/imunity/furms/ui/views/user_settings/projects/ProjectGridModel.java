/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectId;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

class ProjectGridModel {
	public final ProjectId id;
	public final CommunityId communityId;
	public final String name;
	public final String description;
	public final UserStatus status;

	ProjectGridModel(ProjectId id, CommunityId communityId, String name, String description, UserStatus status) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.status = status;
	}

	boolean matches(String value) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		final String lowerCaseValue = value.toLowerCase();
		return name.toLowerCase().contains(lowerCaseValue) ||
				(description != null && description.toLowerCase().contains(lowerCaseValue));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectGridModel that = (ProjectGridModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectGridModel{" +
			"id='" + id + '\'' +
			", communityId='" + communityId + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", status=" + status +
			'}';
	}

	static ProjectGridModelBuilder builder() {
		return new ProjectGridModelBuilder();
	}

	public static final class ProjectGridModelBuilder {
		public ProjectId id;
		public CommunityId communityId;
		public String name;
		public String description;
		public UserStatus status;

		private ProjectGridModelBuilder() {
		}

		public ProjectGridModelBuilder id(ProjectId id) {
			this.id = id;
			return this;
		}

		public ProjectGridModelBuilder communityId(CommunityId communityId) {
			this.communityId = communityId;
			return this;
		}

		public ProjectGridModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectGridModelBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ProjectGridModelBuilder status(UserStatus status) {
			this.status = status;
			return this;
		}

		public ProjectGridModel build() {
			return new ProjectGridModel(id, communityId, name, description, status);
		}
	}
}
