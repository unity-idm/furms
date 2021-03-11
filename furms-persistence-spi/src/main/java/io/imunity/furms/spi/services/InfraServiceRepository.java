/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.services;


import io.imunity.furms.domain.services.InfraService;

import java.util.Optional;
import java.util.Set;

public interface InfraServiceRepository {
	Optional<InfraService> findById(String id);

	Set<InfraService> findAll(String siteId);

	Set<InfraService> findAll();

	String create(InfraService infraService);

	String update(InfraService infraService);

	boolean exists(String id);

	boolean isUniqueName(String name);

	void delete(String id);

	void deleteAll();
}
