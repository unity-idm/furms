/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.sites;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SiteGroupDAO {

	Optional<Site> get(String id);

	void create(Site site);

	void update(Site site);

	void delete(String id);

	List<FURMSUser> getAllSiteUsers(String siteId, Set<Role> roles);

	void addSiteUser(String siteId, PersistentId userId, Role role);

	void removeSiteUser(String siteId, PersistentId userId);
}
