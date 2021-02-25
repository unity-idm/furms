/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.List;
import java.util.Optional;

public interface SiteWebClient {

	Optional<Site> get(String id);

	void create(Site site);

	void update(Site site);

	void delete(String id);

	List<FURMSUser> getAllAdmins(String id);

	boolean isAdmin(String siteId, String userId);

	void addAdmin(String siteId, String userId);

	void removeAdmin(String siteId, String userId);
}
