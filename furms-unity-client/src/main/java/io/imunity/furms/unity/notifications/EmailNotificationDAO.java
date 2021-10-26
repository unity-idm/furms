/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.notifications;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPendingPoliciesChangedEvent;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.unity.client.users.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
class EmailNotificationDAO implements NotificationDAO {

	private static final String NAME_ATTRIBUTE = "custom.name";
	private static final String ROLE_ATTRIBUTE = "custom.role";
	private static final String EMAIL_ATTRIBUTE = "custom.email";
	private static final String PROJECT_ATTRIBUTE = "custom.projectName";
	private static final String URL_ATTRIBUTE = "custom.furmsUrl";
	private static final String POLICY_DOCUMENTS_URL = "/front/users/settings/policy/documents";
	private static final String INVITATIONS_URL = "/front/users/settings/invitations";
	private static final String APPLICATIONS_URL = "/front/project/admin/users?resourceId=";

	private final UserService userService;
	private final UserOperationRepository userOperationRepository;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final EmailNotificationProperties emailNotificationProperties;
	private final ApplicationEventPublisher publisher;
	private final ResourceBundle bundle;

	EmailNotificationDAO(UserService userService,
	                     UserOperationRepository userOperationRepository,
	                     PolicyDocumentDAO policyDocumentDAO,
	                     PolicyDocumentRepository policyDocumentRepository,
	                     EmailNotificationProperties emailNotificationProperties,
	                     ApplicationEventPublisher publisher) {
		this.userService = userService;
		this.userOperationRepository = userOperationRepository;
		this.policyDocumentDAO = policyDocumentDAO;
		this.policyDocumentRepository = policyDocumentRepository;
		this.emailNotificationProperties = emailNotificationProperties;
		this.publisher = publisher;
		this.bundle = ResourceBundle.getBundle("messages", new Locale("en", "US"));
	}

	@Override
	public void notifyUser(PersistentId id, PolicyDocument policyDocument) {
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
	public void notifyAdminAboutRoleRejection(PersistentId id, Role role, String rejectionUserEmail) {
		Map<String, String> attributes = Map.of(ROLE_ATTRIBUTE, bundle.getString(role.name()), EMAIL_ATTRIBUTE, rejectionUserEmail);
		userService.sendUserNotification(id, emailNotificationProperties.rejectedInvitationTemplateId, attributes);
	}

	@Override
	public void notifyAboutChangedPolicy(PolicyDocument policyDocument) {
		policyDocumentDAO.getUserPolicyAcceptances(policyDocument.siteId).stream()
			.filter(userPolicyAcceptances -> userPolicyAcceptances.policyAcceptances.stream()
				.anyMatch(policyAgreement -> policyAgreement.policyDocumentId.equals(policyDocument.id))
			)
			.map(userPolicyAcceptances -> userPolicyAcceptances.user)
			.filter(userPolicyAcceptances -> userPolicyAcceptances.fenixUserId.isPresent())
			.filter(user -> user.id.isPresent())
			.forEach(user -> {
				userService.sendUserNotification(
					user.id.get(),
					emailNotificationProperties.newPolicyRevisionTemplateId,
					Map.of(NAME_ATTRIBUTE, policyDocument.name, URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + POLICY_DOCUMENTS_URL)
				);
				publisher.publishEvent(new UserPendingPoliciesChangedEvent(user.fenixUserId.get()));
			});
	}

	@Override
	public void notifyAllUsersAboutPolicyAssignmentChange(SiteId siteId) {
		final PolicyDocument sitePolicy = policyDocumentRepository.findSitePolicy(siteId.id)
				.orElseThrow(() -> new IllegalArgumentException("Site hasn't policy document attached."));

		notifyAllSiteUsersAboutPolicyAssignmentChange(siteId, sitePolicy);
	}

	@Override
	public void notifyAllUsersAboutPolicyAssignmentChange(InfraService infraService) {
		final PolicyDocument servicePolicy = policyDocumentRepository.findById(infraService.policyId)
				.orElseThrow(() -> new IllegalArgumentException("Service hasn't policy document attached."));

		notifyAllSiteUsersAboutPolicyAssignmentChange(new SiteId(infraService.siteId), servicePolicy);
	}

	@Override
	public void notifyAboutAllNotAcceptedPolicies(String siteId, FenixUserId userId, String grantId) {
		PersistentId persistentId = userService.getPersistentId(userId);

		Map<PolicyId, PolicyAcceptance> policyAcceptanceMap = userService.getPolicyAcceptances(userId).stream()
			.collect(Collectors.toMap(x -> x.policyDocumentId, Function.identity()));

		Optional<PolicyDocument> servicePolicy = policyDocumentRepository.findByUserGrantId(grantId);
		Optional<PolicyDocument> sitePolicy = policyDocumentRepository.findSitePolicy(siteId);

		policyDocumentRepository.findAllByUserId(userId, (id, revision) -> LocalDateTime.MAX).stream()
			.filter(policyDocument ->
				Optional.ofNullable(policyAcceptanceMap.get(policyDocument.id))
					.filter(policyAcceptance -> policyDocument.revision == policyAcceptance.policyDocumentRevision).isEmpty()
			)
			.filter(policyDocument ->
				servicePolicy.filter(policy -> policy.id.equals(policyDocument.id)).isPresent() ||
				sitePolicy.filter(policy -> policy.id.equals(policyDocument.id)).isPresent())
			.forEach(policyDocumentExtended ->
				userService.sendUserNotification(
					persistentId,
					emailNotificationProperties.newPolicyAcceptanceTemplateId,
					Map.of(NAME_ATTRIBUTE, policyDocumentExtended.name, URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + POLICY_DOCUMENTS_URL)
				)
			);
	}

	private void notifyAllSiteUsersAboutPolicyAssignmentChange(SiteId siteId, PolicyDocument policy) {
		Stream.concat(
				userOperationRepository.findAllUserAdditionsBySiteId(siteId.id).stream()
						.map(addition -> addition.userId)
						.map(FenixUserId::new),
				userService.findAllSiteUsers(siteId).stream())
				.distinct()
				.filter(fenixUserId -> policyDocumentDAO.getPolicyAcceptances(fenixUserId).stream()
						.noneMatch(acceptance -> isCurrentRevisionAccepted(policy, acceptance)))
				.forEach(fenixUserId -> {
					final PersistentId persistentId = userService.getPersistentId(fenixUserId);
					userService.sendUserNotification(
							persistentId,
							emailNotificationProperties.newPolicyAcceptanceTemplateId,
							Map.of(NAME_ATTRIBUTE, policy.name,
									URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL + POLICY_DOCUMENTS_URL));
					publisher.publishEvent(new UserPendingPoliciesChangedEvent(fenixUserId));
				});
	}

	private boolean isCurrentRevisionAccepted(PolicyDocument sitePolicy, PolicyAcceptance acceptance) {
		return acceptance.policyDocumentId.equals(sitePolicy.id)
				&& acceptance.policyDocumentRevision == sitePolicy.revision;
	}
}
