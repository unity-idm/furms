/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.notifications;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.notifications.EmailNotificationSender;
import io.imunity.furms.unity.client.users.UserService;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@Component
class EmailNotificationSenderImpl implements EmailNotificationSender {

	private static final String NAME_ATTRIBUTE = "custom.name";
	private static final String ROLE_ATTRIBUTE = "custom.role";
	private static final String EMAIL_ATTRIBUTE = "custom.email";
	private static final String PROJECT_ATTRIBUTE = "custom.projectName";
	private static final String PROJECT_ALLOCATION_ATTRIBUTE = "custom.projectAllocationName";
	private static final String ALARM_ATTRIBUTE = "custom.alarmName";
	private static final String URL_ATTRIBUTE = "custom.furmsUrl";
	private static final String POLICY_DOCUMENTS_URL = "/front/users/settings/policy/documents";
	private static final String INVITATIONS_URL = "/front/users/settings/invitations";
	private static final String QUERY_RESOURCE_PARAM = "?resourceId=";
	private static final String ALLOCATION_CONSUMPTION_URL = "/front/project/admin/resource/allocations/details/";
	private static final String PROJECT_ALLOCATION_CONSUMPTION_URL = "/front/users/settings/project/";
	private static final String APPLICATIONS_URL = "/front/project/admin/users" + QUERY_RESOURCE_PARAM;

	private final UserService userService;
	private final EmailNotificationProperties emailNotificationProperties;
	private final ResourceBundle bundle;

	EmailNotificationSenderImpl(UserService userService,
	                            EmailNotificationProperties emailNotificationProperties) {
		this.userService = userService;
		this.emailNotificationProperties = emailNotificationProperties;
		this.bundle = ResourceBundle.getBundle("messages", new Locale("en", "US"));
	}

	@Override
	public void notifyUserAboutNewPolicy(PersistentId id, PolicyDocument policyDocument) {
		Map<String, String> attributes = Map.of(NAME_ATTRIBUTE, policyDocument.name, URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + POLICY_DOCUMENTS_URL);
		if(policyDocument.revision == 1)
			userService.sendUserNotification(id, emailNotificationProperties.newPolicyAcceptanceTemplateId, attributes);
		else
			userService.sendUserNotification(id, emailNotificationProperties.newPolicyRevisionTemplateId, attributes);
	}

	@Override
	public void notifyUserAboutNewRole(PersistentId id, Role role) {
		Map<String, String> attributes = Map.of(ROLE_ATTRIBUTE, bundle.getString(role.name()), URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + INVITATIONS_URL);
		userService.sendUserNotification(id, emailNotificationProperties.newInvitationTemplateId, attributes);
	}

	@Override
	public void notifyAdminAboutRoleAcceptance(PersistentId id, Role role, String acceptanceUserEmail) {
		Map<String, String> attributes = Map.of(ROLE_ATTRIBUTE, bundle.getString(role.name()), EMAIL_ATTRIBUTE, acceptanceUserEmail);
		userService.sendUserNotification(id, emailNotificationProperties.acceptedInvitationTemplateId, attributes);
	}

	@Override
	public void notifyAdminAboutApplicationRequest(PersistentId id, String projectId, String projectName, String applicationUserEmail) {
		Map<String, String> attributes = Map.of(
			PROJECT_ATTRIBUTE, projectName,
			EMAIL_ATTRIBUTE, applicationUserEmail,
			URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + APPLICATIONS_URL + projectId
		);
		userService.sendUserNotification(id, emailNotificationProperties.newApplicationTemplateId, attributes);
	}

	@Override
	public void notifyUserAboutApplicationAcceptance(PersistentId id, String projectName) {
		Map<String, String> attributes = Map.of(PROJECT_ATTRIBUTE, projectName);
		userService.sendUserNotification(id, emailNotificationProperties.acceptedApplicationTemplateId, attributes);
	}

	@Override
	public void notifyUserAboutApplicationRejection(PersistentId id, String projectName) {
		Map<String, String> attributes = Map.of(PROJECT_ATTRIBUTE, projectName);
		userService.sendUserNotification(id, emailNotificationProperties.rejectedApplicationTemplateId, attributes);
	}

	@Override
	public void notifyProjectAdminAboutResourceUsage(PersistentId id, String projectId, String projectAllocationId, String projectAllocationName, String alarmName) {
		Map<String, String> attributes = Map.of(
			PROJECT_ALLOCATION_ATTRIBUTE, projectAllocationName,
			ALARM_ATTRIBUTE, alarmName,
			URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + ALLOCATION_CONSUMPTION_URL + projectAllocationId + QUERY_RESOURCE_PARAM + projectId
		);
		userService.sendUserNotification(id, emailNotificationProperties.resourceUsageTemplateId, attributes);
	}

	@Override
	public void notifyProjectUserAboutResourceUsage(PersistentId id, String projectId, String projectAllocationId, String projectAllocationName, String alarmName) {
		Map<String, String> attributes = Map.of(
			PROJECT_ALLOCATION_ATTRIBUTE, projectAllocationName,
			ALARM_ATTRIBUTE, alarmName,
			URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + PROJECT_ALLOCATION_CONSUMPTION_URL + projectId
		);
		userService.sendUserNotification(id, emailNotificationProperties.resourceUsageTemplateId, attributes);
	}

	@Override
	public void notifyUserAboutResourceUsage(PersistentId id, String projectId, String projectAllocationId, String projectAllocationName, String alarmName) {
		Map<String, String> attributes = Map.of(
			PROJECT_ALLOCATION_ATTRIBUTE, projectAllocationName,
			ALARM_ATTRIBUTE, alarmName
		);
		userService.sendUserNotification(id, emailNotificationProperties.resourceUsageTemplateWithoutUrlId, attributes);
	}

	@Override
	public void notifyAdminAboutRoleRejection(PersistentId id, Role role, String rejectionUserEmail) {
		Map<String, String> attributes = Map.of(ROLE_ATTRIBUTE, bundle.getString(role.name()), EMAIL_ATTRIBUTE, rejectionUserEmail);
		userService.sendUserNotification(id, emailNotificationProperties.rejectedInvitationTemplateId, attributes);
	}

	@Override
	public void notifyAboutChangedPolicy(PersistentId userId, String policyDocumentName) {
		userService.sendUserNotification(
			userId,
			emailNotificationProperties.newPolicyRevisionTemplateId,
			Map.of(NAME_ATTRIBUTE, policyDocumentName, URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + POLICY_DOCUMENTS_URL)
		);
	}

	@Override
	public void notifyAboutNotAcceptedPolicy(FenixUserId userId, String policyName) {
		userService.sendUserNotification(
			userService.getPersistentId(userId),
			emailNotificationProperties.newPolicyAcceptanceTemplateId,
			Map.of(NAME_ATTRIBUTE, policyName, URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + POLICY_DOCUMENTS_URL)
		);
	}

	@Override
	public void notifySiteUserAboutPolicyAssignmentChange(FenixUserId userId, String policyName) {
		userService.sendUserNotification(
			userService.getPersistentId(userId),
			emailNotificationProperties.newPolicyAcceptanceTemplateId,
			Map.of(NAME_ATTRIBUTE, policyName,
				URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + POLICY_DOCUMENTS_URL)
		);
	}

}
