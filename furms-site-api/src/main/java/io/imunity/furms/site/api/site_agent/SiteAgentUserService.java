/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionJob;
import io.imunity.furms.domain.users.FURMSUser;

public interface SiteAgentUserService {
	void addUser(UserAddition userAddition, FURMSUser user);
	void removeUser(UserAdditionJob userAdditionJob);
}
