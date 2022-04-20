/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_credits;


import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ResourceCreditRepository {
	Optional<ResourceCredit> findById(ResourceCreditId id);

	Set<ResourceCredit> findAll(SiteId siteId);

	Set<ResourceCredit> findAllNotExpiredByResourceTypeId(ResourceTypeId resourceTypeId);

	Set<ResourceCredit> findAll();

	Set<ResourceCredit> findAllByNameOrSiteName(String name);

	Set<ResourceCredit> findAllNotExpiredByNameOrSiteName(String name);

	String create(ResourceCredit resourceCredit);

	void update(ResourceCredit resourceType);

	boolean exists(ResourceCreditId id);

	boolean existsBySiteId(SiteId id);

	boolean existsByResourceTypeId(ResourceTypeId id);

	boolean existsByResourceTypeIdIn(Collection<ResourceTypeId> ids);

	boolean isNamePresent(String name, SiteId siteId);

	void delete(ResourceCreditId id);

	void deleteAll();
}
