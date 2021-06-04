/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_usage;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

public class ProjectAllocationUsage {
	public final String projectId;
	public final String projectAllocationId;
	public final BigDecimal cumulativeConsumption;
	public final ZonedDateTime probedAt;

	ProjectAllocationUsage(String projectId, String projectAllocationId, BigDecimal cumulativeConsumption, ZonedDateTime probedAt) {
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.cumulativeConsumption = cumulativeConsumption;
		this.probedAt = probedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationUsage that = (ProjectAllocationUsage) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(cumulativeConsumption, that.cumulativeConsumption) &&
			Objects.equals(probedAt, that.probedAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, projectAllocationId, cumulativeConsumption, probedAt);
	}

	@Override
	public String toString() {
		return "ProjectAllocationUsage{" +
			"projectId='" + projectId + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", cumulativeConsumption=" + cumulativeConsumption +
			", probedAt=" + probedAt +
			'}';
	}

	public static ProjectAllocationUsageBuilder builder() {
		return new ProjectAllocationUsageBuilder();
	}

	public static final class ProjectAllocationUsageBuilder {
		private String projectId;
		private String projectAllocationId;
		private BigDecimal cumulativeConsumption;
		private ZonedDateTime probedAt;

		private ProjectAllocationUsageBuilder() {
		}

		public ProjectAllocationUsageBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectAllocationUsageBuilder projectAllocationId(String projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public ProjectAllocationUsageBuilder cumulativeConsumption(BigDecimal cumulativeConsumption) {
			this.cumulativeConsumption = cumulativeConsumption;
			return this;
		}

		public ProjectAllocationUsageBuilder probedAt(ZonedDateTime probedAt) {
			this.probedAt = probedAt;
			return this;
		}

		public ProjectAllocationUsage build() {
			return new ProjectAllocationUsage(projectId, projectAllocationId, cumulativeConsumption, probedAt);
		}
	}
}
