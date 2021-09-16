/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.notifications;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPendingPoliciesChangedEvent;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.unity.client.users.UserService;

@Component
class EmailNotificationDAO implements NotificationDAO {

	private static final String NAME_ATTRIBUTE = "custom.name";
	private static final String ROLE_ATTRIBUTE = "custom.role";
	private static final String URL_ATTRIBUTE = "custom.furmsUrl";
	private final UserService userService;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final EmailNotificationProperties emailNotificationProperties;
	private final ApplicationEventPublisher publisher;
	private final ResourceBundle bundle;


	EmailNotificationDAO(UserService userService, PolicyDocumentDAO policyDocumentDAO, PolicyDocumentRepository policyDocumentRepository,
	                     EmailNotificationProperties emailNotificationProperties, ApplicationEventPublisher publisher) {
		this.userService = userService;
		this.policyDocumentDAO = policyDocumentDAO;
		this.policyDocumentRepository = policyDocumentRepository;
		this.emailNotificationProperties = emailNotificationProperties;
		this.publisher = publisher;
		this.bundle = ResourceBundle.getBundle("messages", new Locale("en", "US"));
	}

	@Override
	public void notifyUser(PersistentId id, PolicyDocument policyDocument) {
		Map<String, String> attributes = Map.of(NAME_ATTRIBUTE, policyDocument.name, URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL);
		if(policyDocument.revision == 1)
			userService.sendUserNotification(id, emailNotificationProperties.newPolicyAcceptanceTemplateId, attributes);
		else
			userService.sendUserNotification(id, emailNotificationProperties.newPolicyRevisionTemplateId, attributes);
	}

	@Override
	public void notifyUser(PersistentId id, Invitation invitation) {
		Map<String, String> attributes = Map.of(ROLE_ATTRIBUTE, bundle.getString(invitation.role.name()), URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL);
		userService.sendUserNotification(id, emailNotificationProperties.newInvitationTemplateId, attributes);
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
					Map.of(NAME_ATTRIBUTE, policyDocument.name, URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL)
				);
				publisher.publishEvent(new UserPendingPoliciesChangedEvent(user.fenixUserId.get()));
			});
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
					Map.of(NAME_ATTRIBUTE, policyDocumentExtended.name, URL_ATTRIBUTE, emailNotificationProperties.furmsServerBaseURL)
				)
			);
	}
}
