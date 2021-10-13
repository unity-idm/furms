/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.status_updater;

import io.imunity.furms.domain.users.SiteAgentSetUserAccountStatus;

public interface UserAccountStatusUpdater {
	void setStatus(SiteAgentSetUserAccountStatus userStatus);
}
