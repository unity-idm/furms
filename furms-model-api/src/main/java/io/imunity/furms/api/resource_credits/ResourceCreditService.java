/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_credits;

import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Optional;
import java.util.Set;

public interface ResourceCreditService {
	Optional<ResourceCreditWithAllocations> findWithAllocationsByIdAndSiteId(ResourceCreditId id, SiteId siteId);

	Set<ResourceCreditWithAllocations> findAllWithAllocations(SiteId siteId);

	Set<ResourceCredit> findAllNotExpiredByResourceTypeId(ResourceTypeId resourceTypeId);

	Set<ResourceCredit> findAll();

	Set<ResourceCreditWithAllocations> findAllWithAllocations(String name, boolean fullyDistributed, boolean includedExpired);

	void create(ResourceCredit resourceType);

	void update(ResourceCredit resourceType);

	void delete(ResourceCreditId id, SiteId siteId);

	boolean hasCommunityAllocations(ResourceCreditId id, SiteId siteId);

	Set<String> getOccupiedNames(SiteId siteId);
}
