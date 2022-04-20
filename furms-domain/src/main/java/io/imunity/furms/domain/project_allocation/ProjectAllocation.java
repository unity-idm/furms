/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation;

import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.projects.ProjectId;

import java.math.BigDecimal;
import java.util.Objects;

public class ProjectAllocation {

	public final ProjectAllocationId id;
	public final ProjectId projectId;
	public final CommunityAllocationId communityAllocationId;
	public final String name;
	public final BigDecimal amount;

	private ProjectAllocation(ProjectAllocationId id, ProjectId projectId,
	                          CommunityAllocationId communityAllocationId, String name, BigDecimal amount) {
		this.id = id;
		this.projectId = projectId;
		this.communityAllocationId = communityAllocationId;
		this.name = name;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocation that = (ProjectAllocation) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(communityAllocationId, that.communityAllocationId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, communityAllocationId, name, amount);
	}

	@Override
	public String toString() {
		return "ProjectAllocation{" +
			"id=" + id +
			", projectId=" + projectId +
			", communityAllocationId=" + communityAllocationId +
			", name='" + name + '\'' +
			", amount='" + amount + '\'' +
			'}';
	}

	public static ProjectAllocationBuilder builder() {
		return new ProjectAllocationBuilder();
	}

	public static final class ProjectAllocationBuilder {
		private ProjectAllocationId id;
		private ProjectId projectId;
		private CommunityAllocationId communityAllocationId;
		private String name;
		private BigDecimal amount;

		private ProjectAllocationBuilder() {
		}

		public ProjectAllocationBuilder projectId(String projectId) {
			this.projectId = new ProjectId(projectId);
			return this;
		}

		public ProjectAllocationBuilder communityAllocationId(String communityAllocationId) {
			this.communityAllocationId = new CommunityAllocationId(communityAllocationId);
			return this;
		}

		public ProjectAllocationBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectAllocationBuilder id(String id) {
			this.id = new ProjectAllocationId(id);
			return this;
		}

		public ProjectAllocationBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocation build() {
			return new ProjectAllocation(id, projectId, communityAllocationId, name, amount);
		}
	}
}
