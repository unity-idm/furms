/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.sites;

import io.imunity.furms.domain.sites.Site;

import java.util.Optional;
import java.util.Set;

public interface SiteService {

	Optional<Site> findById(String id);

	Set<Site> findAll();

	void create(Site site);

	void update(Site site);

	void delete(String id);

}
