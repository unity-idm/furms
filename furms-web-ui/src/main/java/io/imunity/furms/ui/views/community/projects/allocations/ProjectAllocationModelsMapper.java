/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;


import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;

class ProjectAllocationModelsMapper {
	static ProjectAllocationViewModel map(ProjectAllocationResolved projectAllocation) {
		return ProjectAllocationViewModel.builder()
			.id(projectAllocation.id)
			.projectId(projectAllocation.projectId)
			.resourceType(new ResourceTypeComboBoxModel(projectAllocation.resourceType.id, projectAllocation.resourceType.name))
			.allocationCommunity(new AllocationCommunityComboBoxModel(projectAllocation.communityAllocation.id, projectAllocation.communityAllocation.name, projectAllocation.resourceCredit.split, projectAllocation.resourceType.unit))
			.name(projectAllocation.name)
			.amount(projectAllocation.amount)
			.build();
	}

	static ProjectAllocation map(ProjectAllocationViewModel projectAllocationViewModel){
		return ProjectAllocation.builder()
			.id(projectAllocationViewModel.id)
			.communityAllocationId(projectAllocationViewModel.getAllocationCommunity().id)
			.projectId(projectAllocationViewModel.projectId)
			.name(projectAllocationViewModel.name)
			.amount(projectAllocationViewModel.amount)
			.build();
	}

	static ProjectAllocationGridModel gridMap(ProjectAllocationResolved projectAllocation) {
		return ProjectAllocationGridModel.builder()
			.id(projectAllocation.id)
			.siteName(projectAllocation.site.getName())
			.resourceTypeName(projectAllocation.resourceType.name)
			.resourceTypeUnit(projectAllocation.resourceType.unit.name())
			.name(projectAllocation.name)
			.amount(projectAllocation.amount)
			.build();
	}
}
