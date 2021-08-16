/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.notifications;

import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.unity.client.users.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
class EmailNotificationDAO implements NotificationDAO {

	private static final String NEW_POLICY_ACCEPTANCE_TEMPLATE_ID = "policyAcceptanceNew";
	private static final String NEW_POLICY_REVISION_TEMPLATE_ID = "policyAcceptanceRevision";
	private static final String NAME_ATTRIBUTE = "custom.name";
	private final UserService userService;
	private final PolicyDocumentDAO policyDocumentDAO;
	private final PolicyDocumentRepository policyDocumentRepository;

	EmailNotificationDAO(UserService userService, PolicyDocumentDAO policyDocumentDAO, PolicyDocumentRepository policyDocumentRepository) {
		this.userService = userService;
		this.policyDocumentDAO = policyDocumentDAO;
		this.policyDocumentRepository = policyDocumentRepository;
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
			.forEach(user -> userService.sendUserNotification(user.id.get(), NEW_POLICY_REVISION_TEMPLATE_ID, Map.of(NAME_ATTRIBUTE, policyDocument.name)));
	}

	@Override
	public void notifyAboutAllNotAcceptedPolicies(FenixUserId userId) {
		PersistentId persistentId = userService.getPersistentId(userId);

		Map<PolicyId, PolicyAcceptance> policyAcceptanceMap = userService.getPolicyAcceptances(userId).stream()
			.collect(Collectors.toMap(x -> x.policyDocumentId, Function.identity()));

		policyDocumentRepository.findAllByUserId(userId, (id, revision) -> LocalDateTime.MAX).stream()
			.filter(policyDocument ->
				ofNullable(policyAcceptanceMap.get(policyDocument.id))
					.filter(policyAcceptance -> policyDocument.revision == policyAcceptance.policyDocumentRevision).isEmpty()
			)
			.forEach(policyDocumentExtended ->
				userService.sendUserNotification(persistentId, NEW_POLICY_ACCEPTANCE_TEMPLATE_ID, Map.of(NAME_ATTRIBUTE, policyDocumentExtended.name))
			);
	}
}
