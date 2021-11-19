/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_site_access;

import io.imunity.furms.domain.resource_access.GrantAccess;

public interface UserSiteAccessInnerService {
	void addAccessToSite(GrantAccess grantAccess);
	void revokeAccessToSite(GrantAccess grantAccess);
}
