/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.ui.components.support.models.allocation.AllocationCommunityComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

class ProjectAllocationComboBoxesModelsResolver {
	private final Set<ResourceTypeComboBoxModel> resourceTypeComboBoxModels;
	private final Map<ResourceTypeId, Set<AllocationCommunityComboBoxModel>> resourceTypeIdToCommunityAllocation;
	private final BiFunction<CommunityId, CommunityAllocationId, BigDecimal> functionAvailableAmount;
	private final ResourceTypeComboBoxModel defaultResourceType;
	private final ResourceTypeId anyId;

	ProjectAllocationComboBoxesModelsResolver(Set<CommunityAllocationResolved> communityAllocations,
											  BiFunction<CommunityId, CommunityAllocationId, BigDecimal> functionAvailableAmount) {
		this.resourceTypeComboBoxModels = communityAllocations.stream()
			.map(allocation -> new ResourceTypeComboBoxModel(allocation.resourceType.id, allocation.resourceType.name))
			.collect(toSet());
		this.anyId = new ResourceTypeId(UUID.randomUUID());
		this.defaultResourceType = new ResourceTypeComboBoxModel(anyId, getTranslation("any"));
		this.resourceTypeComboBoxModels.add(defaultResourceType);

		this.resourceTypeIdToCommunityAllocation = communityAllocations.stream()
			.filter(allocation -> functionAvailableAmount.apply(allocation.communityId, allocation.id)
						.compareTo(BigDecimal.ZERO) > 0)
			.collect(groupingBy(
				allocation -> allocation.resourceType.id,
				mapping(allocation -> new AllocationCommunityComboBoxModel(
					allocation.id,
					allocation.name,
					allocation.resourceCredit.splittable,
					allocation.resourceType.unit),
					toSet()))
			);

		this.functionAvailableAmount = functionAvailableAmount;
	}

	Set<ResourceTypeComboBoxModel> getResourceTypes(){
		return resourceTypeComboBoxModels;
	}

	ResourceTypeComboBoxModel getDefaultResourceType(){
		return defaultResourceType;
	}

	Set<AllocationCommunityComboBoxModel> getCommunityAllocations(ResourceTypeId resourceTypeId){
		if(resourceTypeId.equals(anyId))
			return resourceTypeIdToCommunityAllocation.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		return resourceTypeIdToCommunityAllocation.getOrDefault(resourceTypeId, Set.of());
	}

	BigDecimal getAvailableAmount(CommunityId communityId, CommunityAllocationId communityAllocationId){
		return functionAvailableAmount.apply(communityId, communityAllocationId);
	}
}
