/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_credits;


import io.imunity.furms.domain.resource_credits.ResourceCredit;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ResourceCreditRepository {
	Optional<ResourceCredit> findById(String id);

	Set<ResourceCredit> findAll(String siteId);

	Set<ResourceCredit> findAllNotExpiredByResourceTypeId(String resourceTypeId);

	Set<ResourceCredit> findAll();

	Set<ResourceCredit> findAllByNameOrSiteName(String name);

	Set<ResourceCredit> findAllNotExpiredByNameOrSiteName(String name);

	String create(ResourceCredit resourceType);

	String update(ResourceCredit resourceType);

	boolean exists(String id);

	boolean existsBySiteId(String id);

	boolean existsByResourceTypeId(String id);

	boolean existsByResourceTypeIdIn(Collection<String> ids);

	boolean isNamePresent(String name, String siteId);

	void delete(String id);

	void deleteAll();
}
