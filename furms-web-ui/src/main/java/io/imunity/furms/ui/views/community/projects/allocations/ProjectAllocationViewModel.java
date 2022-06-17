/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.ui.components.support.models.allocation.AllocationCommunityComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;

import java.math.BigDecimal;
import java.util.Objects;

class ProjectAllocationViewModel {
	private ProjectAllocationId id;
	private ProjectId projectId;
	private final String projectName;
	private CommunityId communityId;
	private ResourceTypeComboBoxModel resourceType;
	private AllocationCommunityComboBoxModel allocationCommunity;
	private String name;
	private BigDecimal amount;

	ProjectAllocationViewModel(ProjectId projectId, String projectName) {
		this.projectId = projectId;
		this.projectName = projectName;
	}

	ProjectAllocationViewModel(ProjectAllocationId id,
	                           ProjectId projectId,
	                           String projectName,
	                           CommunityId communityId,
	                           ResourceTypeComboBoxModel resourceType,
	                           AllocationCommunityComboBoxModel allocationCommunity,
	                           String name,
	                           BigDecimal amount) {
		this.id = id;
		this.projectId = projectId;
		this.projectName = projectName;
		this.communityId = communityId;
		this.resourceType = resourceType;
		this.allocationCommunity = allocationCommunity;
		this.name = name;
		this.amount = amount;
	}

	public ProjectAllocationId getId() {
		return id;
	}

	public ProjectId getProjectId() {
		return projectId;
	}

	void setProjectId(ProjectId projectId) {
		this.projectId = projectId;
	}

	String getProjectName() {
		return projectName;
	}

	CommunityId getCommunityId() {
		return communityId;
	}

	ResourceTypeComboBoxModel getResourceType() {
		return resourceType;
	}

	void setResourceType(ResourceTypeComboBoxModel resourceType) {
		this.resourceType = resourceType;
	}

	AllocationCommunityComboBoxModel getAllocationCommunity() {
		return allocationCommunity;
	}

	void setAllocationCommunity(AllocationCommunityComboBoxModel allocationCommunity) {
		this.allocationCommunity = allocationCommunity;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	BigDecimal getAmount() {
		return amount;
	}

	void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationViewModel that = (ProjectAllocationViewModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectAllocationViewModel{" +
				"id='" + id + '\'' +
				", projectId='" + projectId + '\'' +
				", projectName='" + projectName + '\'' +
				", communityId='" + communityId + '\'' +
				", resourceType=" + resourceType +
				", allocationCommunity=" + allocationCommunity +
				", name='" + name + '\'' +
				", amount=" + amount +
				'}';
	}

	public static ProjectAllocationViewModelBuilder builder() {
		return new ProjectAllocationViewModelBuilder();
	}

	public static final class ProjectAllocationViewModelBuilder {
		private ProjectAllocationId id;
		private ProjectId projectId;
		private String projectName;
		private CommunityId communityId;
		private ResourceTypeComboBoxModel resourceType;
		private AllocationCommunityComboBoxModel allocationCommunity;
		private String name;
		private BigDecimal amount;

		private ProjectAllocationViewModelBuilder() {
		}

		public ProjectAllocationViewModelBuilder id(ProjectAllocationId id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationViewModelBuilder projectId(ProjectId projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectAllocationViewModelBuilder projectName(String projectName) {
			this.projectName = projectName;
			return this;
		}

		public ProjectAllocationViewModelBuilder communityId(CommunityId communityId) {
			this.communityId = communityId;
			return this;
		}

		public ProjectAllocationViewModelBuilder resourceType(ResourceTypeComboBoxModel resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public ProjectAllocationViewModelBuilder allocationCommunity(AllocationCommunityComboBoxModel allocationCommunity) {
			this.allocationCommunity = allocationCommunity;
			return this;
		}

		public ProjectAllocationViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectAllocationViewModelBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ProjectAllocationViewModel build() {
			return new ProjectAllocationViewModel(id, projectId, projectName, communityId, resourceType, allocationCommunity, name, amount);
		}
	}
}
