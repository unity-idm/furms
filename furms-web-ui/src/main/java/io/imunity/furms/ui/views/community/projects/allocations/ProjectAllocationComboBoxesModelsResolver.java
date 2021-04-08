/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;
import static java.util.stream.Collectors.*;

class ProjectAllocationComboBoxesModelsResolver {
	private final Set<ResourceTypeComboBoxModel> resourceTypeComboBoxModels;
	private final Map<String, Set<AllocationCommunityComboBoxModel>> resourceTypeIdToCommunityAllocation;
	private final Function<String, BigDecimal> functionAvailableAmount;

	ProjectAllocationComboBoxesModelsResolver(Set<CommunityAllocationResolved> communityAllocations, Function<String, BigDecimal> functionAvailableAmount) {
		this.resourceTypeComboBoxModels = communityAllocations.stream()
			.map(allocation -> new ResourceTypeComboBoxModel(allocation.resourceType.id, allocation.resourceType.name))
			.collect(toSet());
		this.resourceTypeComboBoxModels.add(new ResourceTypeComboBoxModel("ANY", getTranslation("any")));

		this.resourceTypeIdToCommunityAllocation = communityAllocations.stream()
			.collect(groupingBy(
				allocation -> allocation.resourceType.id,
				mapping(allocation -> new AllocationCommunityComboBoxModel(
					allocation.id,
					allocation.name,
					allocation.resourceCredit.split,
					allocation.resourceType.unit),
					toSet()))
			);

		this.functionAvailableAmount = functionAvailableAmount;
	}

	Set<ResourceTypeComboBoxModel> getResourceTypes(){
		return resourceTypeComboBoxModels;
	}

	Set<AllocationCommunityComboBoxModel> getCommunityAllocations(String communityAllocationId){
		if(communityAllocationId.equals("ANY"))
			return resourceTypeIdToCommunityAllocation.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		return resourceTypeIdToCommunityAllocation.getOrDefault(communityAllocationId, Set.of());
	}

	BigDecimal getAvailableAmount(String communityAllocationId){
		return functionAvailableAmount.apply(communityAllocationId);
	}
}
