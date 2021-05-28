/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import java.math.BigDecimal;
import java.util.Objects;

class ProjectAllocationGridModel {
	public String id;
	public String projectId;
	public String siteName;
	public String resourceTypeName;
	public String resourceTypeUnit;
	public String name;
	public BigDecimal amount;
	public boolean accessibleForAllProjectMembers;

	ProjectAllocationGridModel(String id, String projectId, String siteName, String resourceTypeName,
	                           String resourceTypeUnit, String name, BigDecimal amount, boolean accessibleForAllProjectMembers
	) {
		this.id = id;
		this.projectId = projectId;
		this.siteName = siteName;
		this.resourceTypeName = resourceTypeName;
		this.resourceTypeUnit = resourceTypeUnit;
		this.name = name;
		this.amount = amount;
		this.accessibleForAllProjectMembers = accessibleForAllProjectMembers;
	}

	String getSiteName() {
		return siteName;
	}

	String getResourceTypeName() {
		return resourceTypeName;
	}

	String getResourceTypeUnit() {
		return resourceTypeUnit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationGridModel that = (ProjectAllocationGridModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "CommunityAllocationGridModel{" +
			"id='" + id + '\'' +
			", projectId='" + projectId + '\'' +
			", siteName='" + siteName + '\'' +
			", resourceTypeName='" + resourceTypeName + '\'' +
			", resourceTypeUnit='" + resourceTypeUnit + '\'' +
			", name='" + name + '\'' +
			", amount=" + amount +
			", accessibleForAllProjectMembers=" + accessibleForAllProjectMembers +
			'}';
	}

	public static ProjectAllocationGridModelBuilder builder() {
		return new ProjectAllocationGridModelBuilder();
	}

	public static final class ProjectAllocationGridModelBuilder {
		private String id;
		private String projectId;
		private String siteName;
		private String resourceTypeName;
		private String resourceTypeUnit;
		private String name;
		private BigDecimal amount;
		private boolean accessibleForAllProjectMembers;

		private ProjectAllocationGridModelBuilder() {
		}

		public ProjectAllocationGridModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationGridModelBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectAllocationGridModelBuilder accessibleForAllProjectMembers(boolean accessibleForAllProjectMembers) {
			this.accessibleForAllProjectMembers = accessibleForAllProjectMembers;
			return this;
		}

		public ProjectAllocationGridModelBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public ProjectAllocationGridModelBuilder resourceTypeName(String resourceTypeName) {
			this.resourceTypeName = resourceTypeName;
			return this;
		}

		public ProjectAllocationGridModelBuilder resourceTypeUnit(String resourceTypeUnit) {
			this.resourceTypeUnit = resourceTypeUnit;
			return this;
		}

		public ProjectAllocationGridModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectAllocationGridModelBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocationGridModel build() {
			return new ProjectAllocationGridModel(id, projectId, siteName, resourceTypeName, resourceTypeUnit, name, amount, accessibleForAllProjectMembers);
		}
	}
}
