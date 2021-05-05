/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_credits;

import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;

import java.util.Optional;
import java.util.Set;

public interface ResourceCreditService {
	Optional<ResourceCredit> findById(String id);

	Set<ResourceCredit> findAll(String siteId);

	Set<ResourceCredit> findAllByResourceTypeId(String resourceTypeId);

	Set<ResourceCredit> findAll();

	Set<ResourceCreditWithAllocations> findAllWithAllocations(String name, boolean fullyDistributed, boolean includedExpired);

	void create(ResourceCredit resourceType);

	void update(ResourceCredit resourceType);

	void delete(String id);
}
