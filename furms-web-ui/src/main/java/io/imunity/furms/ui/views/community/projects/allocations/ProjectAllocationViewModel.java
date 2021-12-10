/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import io.imunity.furms.ui.components.support.models.allocation.AllocationCommunityComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;

import java.math.BigDecimal;
import java.util.Objects;

class ProjectAllocationViewModel {
	private String id;
	private String projectId;
	private final String projectName;
	private String communityId;
	private ResourceTypeComboBoxModel resourceType;
	private AllocationCommunityComboBoxModel allocationCommunity;
	private String name;
	private BigDecimal amount;

	ProjectAllocationViewModel(String projectId, String projectName) {
		this.projectId = projectId;
		this.projectName = projectName;
	}

	ProjectAllocationViewModel(String id,
	                           String projectId,
	                           String projectName,
	                           String communityId,
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

	public String getId() {
		return id;
	}

	public String getProjectId() {
		return projectId;
	}

	void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	String getProjectName() {
		return projectName;
	}

	void setId(String id) {
		this.id = id;
	}

	String getCommunityId() {
		return communityId;
	}

	void setCommunityId(String communityId) {
		this.communityId = communityId;
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
		private String id;
		private String projectId;
		private String projectName;
		private String communityId;
		private ResourceTypeComboBoxModel resourceType;
		private AllocationCommunityComboBoxModel allocationCommunity;
		private String name;
		private BigDecimal amount;

		private ProjectAllocationViewModelBuilder() {
		}

		public ProjectAllocationViewModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationViewModelBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectAllocationViewModelBuilder projectName(String projectName) {
			this.projectName = projectName;
			return this;
		}

		public ProjectAllocationViewModelBuilder communityId(String communityId) {
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
