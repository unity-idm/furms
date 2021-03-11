/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.services;

import io.imunity.furms.domain.services.InfraService;

import java.util.Optional;
import java.util.Set;

public interface InfraServiceService {
	Optional<InfraService> findById(String id);

	Set<InfraService> findAll(String siteId);

	Set<InfraService> findAll();

	void create(InfraService infraService);

	void update(InfraService infraService);

	void delete(String id);
}
