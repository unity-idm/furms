/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.sites;

import io.imunity.furms.domain.sites.Site;

import java.util.Optional;
import java.util.Set;

public interface SiteRepository {

	Optional<Site> findById(String id);

	Set<Site> findAll();

	String create(Site site);

	String update(Site site);

	boolean exists(String id);

	boolean isUniqueName(String name);

	boolean isUniqueName(String id, String name);

	void delete(String id);
}
