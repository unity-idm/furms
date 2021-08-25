/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.user_operation.UserAddition;

public interface SiteAgentUserService {
	void addUser(UserAddition userAddition, UserPolicyAcceptancesWithServicePolicies userPolicyAcceptances);
	void removeUser(UserAddition userAddition);
}
