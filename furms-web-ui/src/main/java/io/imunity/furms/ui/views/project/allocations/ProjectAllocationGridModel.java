/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.allocations;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_types.AmountWithUnit;
import io.imunity.furms.domain.resource_types.PositiveAmountWithUnit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;

import java.math.BigDecimal;
import java.util.Objects;

class ProjectAllocationGridModel {
	public final ProjectAllocationId id;
	public final ProjectId projectId;
	public final String siteName;
	public final String resourceTypeName;
	public final String name;
	public final AmountWithUnit amountWithUnit;
	public final AmountWithUnit consumedWithUnit;
	public final PositiveAmountWithUnit remainingWithUnit;

	ProjectAllocationGridModel(ProjectAllocationId id, ProjectId projectId, String siteName, String resourceTypeName,
	                           ResourceMeasureUnit resourceTypeUnit, String name, BigDecimal amount, BigDecimal consumed) {
		this.id = id;
		this.projectId = projectId;
		this.siteName = siteName;
		this.resourceTypeName = resourceTypeName;
		this.name = name;
		this.amountWithUnit = new AmountWithUnit(amount, resourceTypeUnit);
		this.consumedWithUnit = new AmountWithUnit(consumed, resourceTypeUnit);
		this.remainingWithUnit = PositiveAmountWithUnit.roundToPositiveValue(amount.subtract(consumed), resourceTypeUnit);
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
			", name='" + name + '\'' +
			", amountWithUnit=" + amountWithUnit +
			'}';
	}

	public static ProjectAllocationGridModelBuilder builder() {
		return new ProjectAllocationGridModelBuilder();
	}

	public static final class ProjectAllocationGridModelBuilder {
		public ProjectAllocationId id;
		public ProjectId projectId;
		public String siteName;
		public String resourceTypeName;
		public ResourceMeasureUnit resourceTypeUnit;
		public String name;
		public BigDecimal amount;
		public BigDecimal consumed;

		private ProjectAllocationGridModelBuilder() {
		}

		public ProjectAllocationGridModelBuilder id(ProjectAllocationId id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationGridModelBuilder projectId(ProjectId projectId) {
			this.projectId = projectId;
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

		public ProjectAllocationGridModelBuilder resourceTypeUnit(ResourceMeasureUnit resourceTypeUnit) {
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

		public ProjectAllocationGridModelBuilder consumed(BigDecimal consumed) {
			this.consumed = consumed;
			return this;
		}

		public ProjectAllocationGridModel build() {
			return new ProjectAllocationGridModel(id, projectId, siteName, resourceTypeName, resourceTypeUnit, name, amount, consumed);
		}
	}
}
