/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.sites;

import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SiteService {

	Optional<Site> findById(String id);

	Set<Site> findAll();

	void create(Site site);

	void update(Site site);

	void delete(String id);

	boolean isNamePresent(String name);

	boolean isNamePresentIgnoringRecord(String name, String recordToIgnore);

	List<FURMSUser> findAllAdmins(String siteId);

	void inviteAdmin(String siteId, PersistentId userId);

	void addAdmin(String siteId, PersistentId userId);

	void removeAdmin(String siteId, PersistentId userId);

	boolean isAdmin(String siteId);

	PendingJob<SiteAgentStatus> getSiteAgentStatus(String siteId);
}
