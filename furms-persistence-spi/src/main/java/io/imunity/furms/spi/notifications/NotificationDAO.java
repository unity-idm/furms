/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.notifications;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;

public interface NotificationDAO {
	void notifyUser(PersistentId id, PolicyDocument policyDocument);
	void notifyUserAboutNewRole(PersistentId id, Role role);
	void notifyAboutChangedPolicy(PolicyDocument policyDocument);
	void notifyAboutAllNotAcceptedPolicies(FenixUserId fenixUserId, String grantId);
}
