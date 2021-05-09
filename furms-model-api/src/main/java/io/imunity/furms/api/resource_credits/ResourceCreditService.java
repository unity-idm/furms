/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_credits;

import java.util.Optional;
import java.util.Set;

import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;

public interface ResourceCreditService {
	Optional<ResourceCredit> findById(String id, String siteId);

	Set<ResourceCredit> findAll(String siteId);

	Set<ResourceCredit> findAllByResourceTypeId(String resourceTypeId);

	Set<ResourceCredit> findAll();

	Set<ResourceCreditWithAllocations> findAllWithAllocations(String name, boolean fullyDistributed, boolean includedExpired);

	void create(ResourceCredit resourceType);

	void update(ResourceCredit resourceType);

	void delete(String id, String siteId);
}
