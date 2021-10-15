/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_site_access;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Map;
import java.util.Optional;

public class UsersSitesAccesses {
	private final Map<String, Map<FenixUserId, UserSiteAccessStatus>> map;

	public UsersSitesAccesses(Map<String, Map<FenixUserId, UserSiteAccessStatus>> map) {
		this.map = map;
	}

	public UserSiteAccessStatus getStatus(String siteId, FenixUserId userId){
		return Optional.ofNullable(map.get(siteId))
			.map(map -> map.getOrDefault(userId, UserSiteAccessStatus.DISABLED))
			.orElse(UserSiteAccessStatus.DISABLED);

	}
}
