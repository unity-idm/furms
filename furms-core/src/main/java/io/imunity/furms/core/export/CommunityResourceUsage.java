/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.export;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;
import java.util.Objects;

import static io.imunity.furms.core.export.CommunityResourceUsage.CommunityResourceUsageBuilder;

@JsonDeserialize(builder = CommunityResourceUsageBuilder.class)
class CommunityResourceUsage {
	public final String allocation;
	public final String allocationId;
	public final String community;
	public final String communityId;
	public final String unit;
	public final List<Consumption> consumption;

	private CommunityResourceUsage(String allocation, String allocationId, String community, String communityId, String unit, List<Consumption> consumption) {
		this.allocation = allocation;
		this.allocationId = allocationId;
		this.community = community;
		this.communityId = communityId;
		this.unit = unit;
		this.consumption = consumption;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityResourceUsage that = (CommunityResourceUsage) o;
		return Objects.equals(allocation, that.allocation) &&
			Objects.equals(allocationId, that.allocationId) &&
			Objects.equals(community, that.community) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(unit, that.unit) &&
			Objects.equals(consumption, that.consumption);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allocation, allocationId, community, communityId, unit, consumption);
	}

	@Override
	public String toString() {
		return "CommunityResourceUsage{" +
			"allocation='" + allocation + '\'' +
			", allocationId='" + allocationId + '\'' +
			", project='" + community + '\'' +
			", projectId='" + communityId + '\'' +
			", unit='" + unit + '\'' +
			", consumption=" + consumption +
			'}';
	}

	public static CommunityResourceUsageBuilder builder() {
		return new CommunityResourceUsageBuilder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	static final class CommunityResourceUsageBuilder {
		private String allocation;
		private String allocationId;
		private String project;
		private String projectId;
		private String unit;
		private List<Consumption> consumption;

		private CommunityResourceUsageBuilder() {
		}

		public CommunityResourceUsageBuilder allocation(String allocation) {
			this.allocation = allocation;
			return this;
		}

		public CommunityResourceUsageBuilder allocationId(String allocationId) {
			this.allocationId = allocationId;
			return this;
		}

		public CommunityResourceUsageBuilder community(String project) {
			this.project = project;
			return this;
		}

		public CommunityResourceUsageBuilder communityId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public CommunityResourceUsageBuilder unit(String unit) {
			this.unit = unit;
			return this;
		}

		public CommunityResourceUsageBuilder consumption(List<Consumption> consumption) {
			this.consumption = consumption;
			return this;
		}

		public CommunityResourceUsage build() {
			return new CommunityResourceUsage(allocation, allocationId, project, projectId, unit, consumption);
		}
	}
}
