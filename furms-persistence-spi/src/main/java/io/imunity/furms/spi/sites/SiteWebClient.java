/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.sites;

import io.imunity.furms.domain.sites.Site;

public interface SiteWebClient {

	Site get(String id);

	void create(Site site);

	void delete(String id);

}
