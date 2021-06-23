/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.project;

import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;

import java.util.Objects;

public class ProjectViewGridModel {
	public final String id;
	public final String communityId;
	public final String name;
	public final String siteName;
	public final String description;
	public final ProjectInstallationStatus status;

	ProjectViewGridModel(String id, String communityId, String name, String siteName, String description, ProjectInstallationStatus status) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.siteName = siteName;
		this.description = description;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectViewGridModel that = (ProjectViewGridModel) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(siteName, that.siteName) &&
			Objects.equals(description, that.description) &&
			Objects.equals(status, that.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityId, name, siteName, description, status);
	}

	@Override
	public String toString() {
		return "ProjectViewGridModel{" +
			"id='" + id + '\'' +
			", communityId='" + communityId + '\'' +
			", name='" + name + '\'' +
			", siteName='" + siteName + '\'' +
			", description='" + description + '\'' +
			", status=" + status +
			'}';
	}

	public boolean matches(String value) {
		return name.toLowerCase().contains(value) ||
			description.toLowerCase().contains(value) ||
			siteName.toLowerCase().contains(value) ||
			status.toString().toLowerCase().contains(value);
	}

	public static ProjectViewGridModelBuilder builder() {
		return new ProjectViewGridModelBuilder();
	}

	public static final class ProjectViewGridModelBuilder {
		public String id;
		public String communityId;
		public String name;
		public String siteName;
		public String description;
		public ProjectInstallationStatus status;

		private ProjectViewGridModelBuilder() {
		}

		public ProjectViewGridModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectViewGridModelBuilder communityId(String id) {
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

		public ProjectViewGridModelBuilder status(ProjectInstallationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectViewGridModel build() {
			return new ProjectViewGridModel(id, communityId, name, siteName, description, status);
		}
	}
}
