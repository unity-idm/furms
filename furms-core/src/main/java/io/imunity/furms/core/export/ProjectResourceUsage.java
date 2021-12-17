/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.export;

import java.util.List;
import java.util.Objects;

class ProjectResourceUsage {
	public final String allocation;
	public final String allocationId;
	public final String project;
	public final String projectId;
	public final String unit;
	public final List<Consumption> consumption;

	private ProjectResourceUsage(String allocation, String allocationId, String project, String projectId, String unit, List<Consumption> consumption) {
		this.allocation = allocation;
		this.allocationId = allocationId;
		this.project = project;
		this.projectId = projectId;
		this.unit = unit;
		this.consumption = consumption;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectResourceUsage that = (ProjectResourceUsage) o;
		return Objects.equals(allocation, that.allocation) &&
			Objects.equals(allocationId, that.allocationId) &&
			Objects.equals(project, that.project) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(unit, that.unit) &&
			Objects.equals(consumption, that.consumption);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allocation, allocationId, project, projectId, unit, consumption);
	}

	@Override
	public String toString() {
		return "ProjectResourceUsage{" +
			"allocation='" + allocation + '\'' +
			", allocationId='" + allocationId + '\'' +
			", project='" + project + '\'' +
			", projectId='" + projectId + '\'' +
			", unit='" + unit + '\'' +
			", consumption=" + consumption +
			'}';
	}

	public static ProjectResourceUsageBuilder builder() {
		return new ProjectResourceUsageBuilder();
	}

	public static final class ProjectResourceUsageBuilder {
		public String allocation;
		public String allocationId;
		public String project;
		public String projectId;
		public String unit;
		public List<Consumption> consumption;

		private ProjectResourceUsageBuilder() {
		}

		public ProjectResourceUsageBuilder allocation(String allocation) {
			this.allocation = allocation;
			return this;
		}

		public ProjectResourceUsageBuilder allocationId(String allocationId) {
			this.allocationId = allocationId;
			return this;
		}

		public ProjectResourceUsageBuilder project(String project) {
			this.project = project;
			return this;
		}

		public ProjectResourceUsageBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectResourceUsageBuilder unit(String unit) {
			this.unit = unit;
			return this;
		}

		public ProjectResourceUsageBuilder consumption(List<Consumption> consumption) {
			this.consumption = consumption;
			return this;
		}

		public ProjectResourceUsage build() {
			return new ProjectResourceUsage(allocation, allocationId, project, projectId, unit, consumption);
		}
	}
}
