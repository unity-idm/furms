/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.services;

import io.imunity.furms.domain.services.Service;

import java.util.Optional;
import java.util.Set;

public interface ServiceService {
	Optional<Service> findById(String id);

	Set<Service> findAll(String siteId);

	Set<Service> findAll();

	void create(Service service);

	void update(Service service);

	void delete(String id);
}
