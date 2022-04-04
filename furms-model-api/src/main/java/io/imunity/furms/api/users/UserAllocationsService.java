/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.users;

import io.imunity.furms.domain.sites.UserSitesInstallationInfoData;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.sites.SiteUser;

import java.util.Set;

public interface UserAllocationsService {
	Set<UserSitesInstallationInfoData> findCurrentUserSitesInstallations();

	Set<SiteUser> findUserSitesInstallations(PersistentId userId);

	Set<UserAddition> findUserAdditionsByFenixUserId(FenixUserId userId);

	Set<UserAddition> findUserAdditionsBySiteAndFenixUserId(String siteId, FenixUserId fenixUserId);

	Set<UserAddition> findUserAdditionsBySiteId(String siteId);

	Set<UserAddition> findUserAdditionsByProjectId(String projectId);
}
