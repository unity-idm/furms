/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

class ProjectViewGridModel {
	public final String id;
	public final String projectId;
	public final String communityId;
	public final String name;
	public final String siteName;
	public final String description;
	public final String status;
	public final String message;
	public final boolean expired;

	ProjectViewGridModel(String id, String projectId, String communityId, String name, String siteName,
	                     String description, String status, String message, boolean expired) {
		this.id = id;
		this.projectId = projectId;
		this.communityId = communityId;
		this.name = name;
		this.siteName = siteName;
		this.description = description;
		this.status = status;
		this.message = message;
		this.expired = expired;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectViewGridModel that = (ProjectViewGridModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectViewGridModel{" +
			"projectId='" + projectId + '\'' +
			", communityId='" + communityId + '\'' +
			", name='" + name + '\'' +
			", siteName='" + siteName + '\'' +
			", description='" + description + '\'' +
			", status=" + status +
			", message=" + message +
			", expired=" + expired +
			'}';
	}

	public boolean matches(String value) {
		return StringUtils.isBlank(value) ||
				name.toLowerCase().contains(value.toLowerCase()) ||
				(description != null && description.toLowerCase().contains(value.toLowerCase())) ||
				(siteName != null && siteName.toLowerCase().contains(value.toLowerCase())) ||
				(status != null && status.toString().toLowerCase().contains(value.toLowerCase()));
	}

	public static ProjectViewGridModelBuilder builder() {
		return new ProjectViewGridModelBuilder();
	}

	public static final class ProjectViewGridModelBuilder {
		public String id;
		public String projectId;
		public String communityId;
		public String name;
		public String siteName;
		public String description;
		public String status;
		public String message;
		public boolean expired;

		private ProjectViewGridModelBuilder() {
		}

		public ProjectViewGridModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectViewGridModelBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectViewGridModelBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public ProjectViewGridModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectViewGridModelBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public ProjectViewGridModelBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ProjectViewGridModelBuilder status(String status) {
			this.status = status;
			return this;
		}

		public ProjectViewGridModelBuilder message(String message) {
			this.message = message;
			return this;
		}

		public ProjectViewGridModelBuilder expired(boolean expired) {
			this.expired = expired;
			return this;
		}

		public ProjectViewGridModel build() {
			return new ProjectViewGridModel(id, projectId, communityId, name, siteName, description, status, message, expired);
		}
	}
}
