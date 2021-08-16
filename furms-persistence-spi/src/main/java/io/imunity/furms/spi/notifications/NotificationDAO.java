/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.notifications;

import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.users.FenixUserId;

public interface NotificationDAO {
	void notifyAboutChangedPolicy(PolicyDocument policyDocument);
	void notifyAboutAllNotAcceptedPolicies(FenixUserId fenixUserId);
}
