/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface SiteRepository {

	Optional<Site> findById(String id);

	SiteExternalId findByIdExternalId(String id);

	Set<SiteId> findByProjectId(String id);

	Map<String, Set<String>> findRelatedProjectIds(SiteId siteId);

	Set<Site> findAll();

	String create(Site site, SiteExternalId externalId);

	String update(Site site);

	boolean exists(String id);

	boolean existsByExternalId(SiteExternalId siteExternalId);

	boolean isNamePresent(String name);

	boolean isNamePresentIgnoringRecord(String name, String recordToIgnore);

	void delete(String id);
	
	void deleteAll();
}
