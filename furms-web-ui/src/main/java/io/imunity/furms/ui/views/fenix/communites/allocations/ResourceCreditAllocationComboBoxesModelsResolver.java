/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.sites.Site;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

class ResourceCreditAllocationComboBoxesModelsResolver {
	private final Set<SiteComboBoxModel> sites;
	private final Function<String, Set<ResourceType>> functionResourceType;
	private final Function<String, Set<ResourceCredit>> functionResourceCredit;

	ResourceCreditAllocationComboBoxesModelsResolver(Set<Site> sites, Function<String, Set<ResourceType>> functionResourceType, Function<String, Set<ResourceCredit>> functionResourceCredit) {
		this.sites = sites.stream().map(s -> new SiteComboBoxModel(s.getId(), s.getName())).collect(Collectors.toSet());
		this.functionResourceType = functionResourceType;
		this.functionResourceCredit = functionResourceCredit;
	}

	Set<ResourceTypeComboBoxModel> getResourceTypes(String siteId){
		return functionResourceType.apply(siteId).stream()
			.map(r -> new ResourceTypeComboBoxModel(r.id, r.name, r.unit))
			.collect(toSet());
	}

	Set<ResourceCreditComboBoxModel> getResourceCredits(String resourceTypeId){
		return functionResourceCredit.apply(resourceTypeId).stream()
			.map(r -> new ResourceCreditComboBoxModel(r.id, r.name, r.amount, r.split))
			.collect(toSet());
	}

	Set<SiteComboBoxModel> getSites(){
		return sites;
	}
}
