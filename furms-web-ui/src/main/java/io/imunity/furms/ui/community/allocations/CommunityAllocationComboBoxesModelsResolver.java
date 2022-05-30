/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.community.allocations;

import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.ui.components.support.models.SiteComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceCreditComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class CommunityAllocationComboBoxesModelsResolver {
	private final Set<SiteComboBoxModel> sites;
	private final Function<SiteId, Set<ResourceType>> functionResourceType;
	private final Function<ResourceTypeId, Set<ResourceCredit>> functionResourceCredit;
	private final Function<ResourceCreditId, BigDecimal> functionAvailableAmount;

	public CommunityAllocationComboBoxesModelsResolver(Set<Site> sites,
	                                                   Function<SiteId, Set<ResourceType>> functionResourceType,
	                                                   Function<ResourceTypeId, Set<ResourceCredit>> functionResourceCredit,
	                                                   Function<ResourceCreditId, BigDecimal> functionAvailableAmount) {
		this.sites = sites.stream().map(s -> new SiteComboBoxModel(s.getId(), s.getName())).collect(Collectors.toSet());
		this.functionResourceType = functionResourceType;
		this.functionResourceCredit = functionResourceCredit;
		this.functionAvailableAmount = functionAvailableAmount;
	}

	public Set<ResourceTypeComboBoxModel> getResourceTypes(SiteId siteId) {
		if (siteId == null)
			return Set.of();
		return functionResourceType.apply(siteId).stream()
				.map(r -> new ResourceTypeComboBoxModel(r.id, r.name, r.unit))
				.collect(toSet());
	}

	public Set<ResourceCreditComboBoxModel> getResourceCredits(ResourceTypeId resourceTypeId) {
		if (resourceTypeId == null)
			return Set.of();
		return functionResourceCredit.apply(resourceTypeId).stream()
				.filter(r -> getAvailableAmount(r.id).compareTo(BigDecimal.ZERO) > 0)
				.map(r -> new ResourceCreditComboBoxModel(r.id, r.name, r.amount, r.splittable))
				.collect(toSet());
	}

	public Set<SiteComboBoxModel> getSites() {
		return sites;
	}

	public BigDecimal getAvailableAmount(ResourceCreditId resourceCreditId) {
		return functionAvailableAmount.apply(resourceCreditId);
	}
}
