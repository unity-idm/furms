/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.services;

import java.util.Optional;
import java.util.Set;

import io.imunity.furms.domain.services.InfraService;

public interface InfraServiceService {
	Optional<InfraService> findById(String id, String siteId);

	Set<InfraService> findAll(String siteId);

	Set<InfraService> findAll();

	void create(InfraService infraService);

	void update(InfraService infraService);

	void delete(String infraServiceId, String siteId);
}
