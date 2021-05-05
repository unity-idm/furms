/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.PersistentId;

public interface UserOperationService {
	void createUserAdditions(String projectId, PersistentId userId);
	void createUserAdditions(SiteId siteId, String communityId, String projectId);
	void createUserRemovals(String projectId, PersistentId userId);
	boolean isUserAdded(String siteId, String userId);
}
