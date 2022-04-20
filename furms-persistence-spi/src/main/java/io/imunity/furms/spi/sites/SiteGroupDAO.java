/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.sites;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.AllUsersAndSiteAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SiteGroupDAO {

	Optional<Site> get(SiteId id);

	void create(Site site);

	void update(Site site);

	void delete(SiteId id);

	List<FURMSUser> getSiteUsers(SiteId siteId, Set<Role> roles);

	AllUsersAndSiteAdmins getAllUsersAndSiteAdmins(SiteId siteId);

	void addSiteUser(SiteId siteId, PersistentId userId, Role role);

	void removeSiteUser(SiteId siteId, PersistentId userId);
}
