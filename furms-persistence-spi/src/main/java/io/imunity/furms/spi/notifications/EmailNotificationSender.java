/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.notifications;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;

public interface EmailNotificationSender {
	void notifyUserAboutNewPolicy(PersistentId id, PolicyDocument policyDocument);
	void notifyAboutChangedPolicy(PersistentId userId, String policyDocumentName);
	void notifyAboutNotAcceptedPolicy(FenixUserId userId, String policyName);
	void notifySiteUserAboutPolicyAssignmentChange(FenixUserId userId, String policyName);

	void notifyUserAboutNewRole(PersistentId id, Role role);
	void notifyAdminAboutRoleAcceptance(PersistentId id, Role role, String acceptanceUserEmail);
	void notifyAdminAboutRoleRejection(PersistentId id, Role role, String rejectionUserEmail);

	void notifyAdminAboutApplicationRequest(PersistentId id, ProjectId projectId, String projectName,
	                                        String applicationUserEmail);
	void notifyUserAboutApplicationAcceptance(PersistentId id, String projectName);
	void notifyUserAboutApplicationRejection(PersistentId id, String projectName);

	void notifyProjectAdminAboutResourceUsage(PersistentId id, ProjectId projectId, ProjectAllocationId projectAllocationId,
	                                          String projectAllocationName, String alarmName);
	void notifyProjectUserAboutResourceUsage(PersistentId id, ProjectId projectId, ProjectAllocationId projectAllocationId, String projectAllocationName, String alarmName);
	void notifyUserAboutResourceUsage(PersistentId id, ProjectId projectId, ProjectAllocationId projectAllocationId, String projectAllocationName, String alarmName);
}
