/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.user_site_access;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Map;
import java.util.Set;

public interface UserSiteAccessRepository {
	Set<String> findAllUserProjectIds(String siteId, FenixUserId userId);
	Map<String, Set<FenixUserId>> findAllUserGroupedBySiteId(String projectId);
	void add(String siteId, String projectId, FenixUserId userId);
	void remove(String siteId, String projectId, FenixUserId userId);
	void remove(String projectId, FenixUserId userId);
	boolean exists(String siteId, String projectId, FenixUserId userId);
}
