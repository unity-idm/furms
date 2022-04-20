/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.services;


import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Optional;
import java.util.Set;

public interface InfraServiceRepository {
	Optional<InfraService> findById(InfraServiceId id);

	Set<InfraService> findAll(SiteId siteId);

	Set<InfraService> findAll();

	String create(InfraService infraService);

	void update(InfraService infraService);

	boolean exists(InfraServiceId id);

	boolean isNamePresent(String name, SiteId siteId);

	void delete(InfraServiceId id);

	void deleteAll();
}
