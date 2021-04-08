/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import java.math.BigDecimal;
import java.util.Objects;

class ProjectAllocationViewModel {
	public String id;
	public String projectId;
	public ResourceTypeComboBoxModel resourceType;
	public AllocationCommunityComboBoxModel allocationCommunity;
	public String name;
	public BigDecimal amount;

	ProjectAllocationViewModel(String id, String projectId,
	                           ResourceTypeComboBoxModel resourceType,
	                           AllocationCommunityComboBoxModel allocationCommunity, String name, BigDecimal amount) {
		this.id = id;
		this.projectId = projectId;
		this.resourceType = resourceType;
		this.allocationCommunity = allocationCommunity;
		this.name = name;
		this.amount = amount;
	}

	void setProjectId(String projectId) {
		this.projectId = projectId;
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
			", resourceTypeId='" + resourceType + '\'' +
			", resourceCreditId='" + allocationCommunity + '\'' +
			", name='" + name + '\'' +
			", amount=" + amount +
			'}';
	}

	public ProjectAllocationViewModel() {}

	public static ProjectAllocationViewModelBuilder builder() {
		return new ProjectAllocationViewModelBuilder();
	}

	public static final class ProjectAllocationViewModelBuilder {
		public String id;
		public String projectId;
		public ResourceTypeComboBoxModel resourceType;
		public AllocationCommunityComboBoxModel allocationCommunity;
		public String name;
		public BigDecimal amount;

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
			return new ProjectAllocationViewModel(id, projectId, resourceType, allocationCommunity, name, amount);
		}
	}
}
