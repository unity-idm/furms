/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import java.util.Objects;

class ProjectGridModel {
	public final String id;
	public final String communityId;
	public final String name;
	public final String description;
	public final UserStatus status;

	ProjectGridModel(String id, String communityId, String name, String description, UserStatus status) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.status = status;
	}

	boolean matches(String value) {
		return name.toLowerCase().contains(value) ||
				(description != null && description.toLowerCase().contains(value));
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
		public String id;
		public String communityId;
		public String name;
		public String description;
		public UserStatus status;

		private ProjectGridModelBuilder() {
		}

		public ProjectGridModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectGridModelBuilder communityId(String communityId) {
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
