/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import io.imunity.furms.domain.resource_types.AmountWithUnit;
import io.imunity.furms.domain.resource_types.PositiveAmountWithUnit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

class ProjectAllocationGridModel {
	public final String id;
	public final String projectId;
	public final String siteName;
	public final String resourceTypeName;
	public final String name;
	public final AmountWithUnit amountWithUnit;
	public final AmountWithUnit consumedWithUnit;
	public final PositiveAmountWithUnit remainingWithUnit;
	public final LocalDateTime creationTime;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;

	ProjectAllocationGridModel(String id, String projectId, String siteName, String resourceTypeName,
	                           ResourceMeasureUnit resourceTypeUnit, String name, BigDecimal amount, BigDecimal consumed,
	                           LocalDateTime creationTime, LocalDateTime validFrom, LocalDateTime validTo) {
		this.id = id;
		this.projectId = projectId;
		this.siteName = siteName;
		this.resourceTypeName = resourceTypeName;
		this.name = name;
		this.amountWithUnit = new AmountWithUnit(amount, resourceTypeUnit);
		this.consumedWithUnit = new AmountWithUnit(consumed, resourceTypeUnit);
		this.remainingWithUnit = PositiveAmountWithUnit.roundToPositiveValue(amount.subtract(consumed), resourceTypeUnit);
		this.creationTime = creationTime;
		this.validFrom = validFrom;
		this.validTo = validTo;
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
			", creationTime=" + creationTime +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
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
		private ResourceMeasureUnit resourceTypeUnit;
		private String name;
		private BigDecimal amount;
		private BigDecimal consumed;
		private LocalDateTime creationTime;
		private LocalDateTime validFrom;
		private LocalDateTime validTo;

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

		public ProjectAllocationGridModelBuilder creationTime(LocalDateTime creationTime) {
			this.creationTime = creationTime;
			return this;
		}

		public ProjectAllocationGridModelBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public ProjectAllocationGridModelBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public ProjectAllocationGridModel build() {
			return new ProjectAllocationGridModel(id, projectId, siteName, resourceTypeName, resourceTypeUnit, name, amount, consumed, creationTime, validFrom, validTo);
		}
	}
}
