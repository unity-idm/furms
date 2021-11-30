/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.notifications;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;

public interface EmailNotificationDAO {
	void notifyUserAboutNewPolicy(PersistentId id, PolicyDocument policyDocument);
	void notifyAboutChangedPolicy(PersistentId userId, String policyDocumentName);
	void notifyAboutNotAcceptedPolicy(FenixUserId userId, String policyName);
	void notifySiteUserAboutPolicyAssignmentChange(FenixUserId userId, String policyName);

	void notifyUserAboutNewRole(PersistentId id, Role role);
	void notifyAdminAboutRoleAcceptance(PersistentId id, Role role, String acceptanceUserEmail);
	void notifyAdminAboutRoleRejection(PersistentId id, Role role, String rejectionUserEmail);

	void notifyAdminAboutApplicationRequest(PersistentId id, String projectId, String projectName, String applicationUserEmail);
	void notifyUserAboutApplicationAcceptance(PersistentId id, String projectName);
	void notifyUserAboutApplicationRejection(PersistentId id, String projectName);

	void notifyAdminAboutResourceUsage(PersistentId id, String projectId, String projectAllocationId, String projectAllocationName, String alarmName);
}
