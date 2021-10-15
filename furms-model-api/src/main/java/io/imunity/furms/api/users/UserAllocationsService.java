/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.users;

import io.imunity.furms.domain.sites.UserSitesInstallationInfoData;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.sites.SiteUser;

import java.util.Set;

public interface UserAllocationsService {
	Set<UserSitesInstallationInfoData> findCurrentUserSitesInstallations();

	Set<SiteUser> findUserSitesInstallations(PersistentId userId);

	Set<UserAddition> findAllBySiteId(String siteId);

	Set<UserAddition> findAllByProjectId(String projectId);
}
