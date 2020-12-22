/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.sites;

import io.imunity.furms.domain.sites.Site;

import java.util.Optional;

public interface SiteWebClient {

	Optional<Site> get(String id);

	void create(Site site);

	void update(Site site);

	void delete(String id);

}
