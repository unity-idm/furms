/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_usage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ResourceUsage {
	public final String projectId;
	public final String projectAllocationId;
	public final BigDecimal cumulativeConsumption;
	public final LocalDateTime utcProbedAt;

	ResourceUsage(String projectId, String projectAllocationId, BigDecimal cumulativeConsumption, LocalDateTime utcProbedAt) {
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.cumulativeConsumption = cumulativeConsumption;
		this.utcProbedAt = utcProbedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceUsage that = (ResourceUsage) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(cumulativeConsumption, that.cumulativeConsumption) &&
			Objects.equals(utcProbedAt, that.utcProbedAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, projectAllocationId, cumulativeConsumption, utcProbedAt);
	}

	@Override
	public String toString() {
		return "ProjectAllocationUsage{" +
			"projectId='" + projectId + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", cumulativeConsumption=" + cumulativeConsumption +
			", probedAt=" + utcProbedAt +
			'}';
	}

	public static ProjectAllocationUsageBuilder builder() {
		return new ProjectAllocationUsageBuilder();
	}

	public static final class ProjectAllocationUsageBuilder {
		private String projectId;
		private String projectAllocationId;
		private BigDecimal cumulativeConsumption;
		private LocalDateTime probedAt;

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

		public ProjectAllocationUsageBuilder probedAt(LocalDateTime probedAt) {
			this.probedAt = probedAt;
			return this;
		}

		public ResourceUsage build() {
			return new ResourceUsage(projectId, projectAllocationId, cumulativeConsumption, probedAt);
		}
	}
}
