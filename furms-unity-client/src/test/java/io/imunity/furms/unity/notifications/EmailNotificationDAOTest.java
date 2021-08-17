/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.notifications;

import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.unity.client.users.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EmailNotificationDAOTest {

	@Mock
	private UserService userService;
	@Mock
	private PolicyDocumentDAO policyDocumentDAO;
	@Mock
	private PolicyDocumentRepository policyDocumentRepository;
	@Mock
	private ApplicationEventPublisher publisher;

	@InjectMocks
	private EmailNotificationDAO emailNotificationDAO;

	@Test
	void shouldNotifyAboutChangedPolicy() {
		PersistentId id = new PersistentId(UUID.randomUUID().toString());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		String siteId = "siteId";
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId(siteId)
			.name("policyName")
			.build();

		FURMSUser user = FURMSUser.builder()
			.id(id)
			.fenixUserId("fenixUserId")
			.email("email")
			.build();

		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.build();

		UserPolicyAcceptances userPolicyAcceptances = new UserPolicyAcceptances(user, Set.of(policyAcceptance));

		when(policyDocumentDAO.getUserPolicyAcceptances(siteId))
			.thenReturn(Set.of(userPolicyAcceptances));

		emailNotificationDAO.notifyAboutChangedPolicy(policyDocument);

		verify(userService).sendUserNotification(id, "policyAcceptanceRevision", Map.of("custom.name", "policyName"));
	}

	@Test
	void shouldNotNotifyAboutChangedPolicy() {
		PersistentId id = new PersistentId(UUID.randomUUID().toString());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		String siteId = "siteId";
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId(siteId)
			.name("policyName")
			.build();

		FURMSUser user = FURMSUser.builder()
			.id(id)
			.fenixUserId("fenixUserId")
			.email("email")
			.build();

		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(new PolicyId(UUID.randomUUID()))
			.build();

		UserPolicyAcceptances userPolicyAcceptances = new UserPolicyAcceptances(user, Set.of(policyAcceptance));

		when(policyDocumentDAO.getUserPolicyAcceptances(siteId))
			.thenReturn(Set.of(userPolicyAcceptances));

		emailNotificationDAO.notifyAboutChangedPolicy(policyDocument);

		verify(userService, times(0)).sendUserNotification(id, "policyAcceptanceRevision", Map.of("custom.name", "policyName"));
	}

	@Test
	void shouldNotifyAboutNewPolicy() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.build();

		PolicyDocumentExtended policyDocumentExtended = PolicyDocumentExtended.builder()
			.name("policyName")
			.build();

		when(userService.getPersistentId(fenixUserId)).thenReturn(id);
		when(userService.getPolicyAcceptances(fenixUserId)).thenReturn(Set.of(policyAcceptance));
		when(policyDocumentRepository.findAllByUserId(eq(fenixUserId), any())).thenReturn(Set.of(policyDocumentExtended));


		emailNotificationDAO.notifyAboutAllNotAcceptedPolicies(fenixUserId);

		verify(userService).sendUserNotification(id, "policyAcceptanceNew", Map.of("custom.name", "policyName"));
	}

	@Test
	void shouldNotNotifyAboutNewPolicy() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.policyDocumentRevision(1)
			.build();

		PolicyDocumentExtended policyDocumentExtended = PolicyDocumentExtended.builder()
			.id(policyId)
			.name("policyName")
			.revision(1)
			.build();

		when(userService.getPersistentId(fenixUserId)).thenReturn(id);
		when(userService.getPolicyAcceptances(fenixUserId)).thenReturn(Set.of(policyAcceptance));
		when(policyDocumentRepository.findAllByUserId(eq(fenixUserId), any())).thenReturn(Set.of(policyDocumentExtended));


		emailNotificationDAO.notifyAboutAllNotAcceptedPolicies(fenixUserId);

		verify(userService, times(0)).sendUserNotification(id, "policyAcceptanceNew", Map.of("custom.name", "policyName"));
	}
}