/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.services;


import io.imunity.furms.domain.services.Service;

import java.util.Optional;
import java.util.Set;

public interface ServiceRepository {
	Optional<Service> findById(String id);

	Set<Service> findAll(String siteId);

	Set<Service> findAll();

	String create(Service service);

	String update(Service service);

	boolean exists(String id);

	boolean isUniqueName(String name);

	void delete(String id);

	void deleteAll();
}
