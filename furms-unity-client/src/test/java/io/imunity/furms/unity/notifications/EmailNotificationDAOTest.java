/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.notifications;

import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.unity.client.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EmailNotificationDAOTest {
	private static final String FURMS_BASE_URL = "https://localhost:333";
	private static final String POLICY_DOCUMENT_BASE_URL = FURMS_BASE_URL + "/front/users/settings/policy/documents";
	@Mock
	private UserService userService;
	@Mock
	private PolicyDocumentDAO policyDocumentDAO;
	@Mock
	private PolicyDocumentRepository policyDocumentRepository;
	@Mock
	private UserOperationRepository userOperationRepository;
	@Mock
	private ApplicationEventPublisher publisher;

	private EmailNotificationDAO emailNotificationDAO;

	@BeforeEach
	void setUp() {
		EmailNotificationProperties emailNotificationProperties = new EmailNotificationProperties(
			"policyAcceptanceNew",
			"policyAcceptanceRevision",
			"newInvitation",
			"acceptedInvitation",
			"rejectedInvitation",
			"newApplication",
			"acceptedApplication",
			"rejectedApplication",
				FURMS_BASE_URL);
		emailNotificationDAO = new EmailNotificationDAO(userService, userOperationRepository, policyDocumentDAO, policyDocumentRepository, emailNotificationProperties, publisher);
	}

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

		verify(userService).sendUserNotification(id, "policyAcceptanceRevision",
			Map.of("custom.name", "policyName",
				"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL)
		);
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

		verify(userService, times(0)).sendUserNotification(id, "policyAcceptanceRevision", Map.of("custom.name", "policyName", "custom.furmsUrl", FURMS_BASE_URL));
	}

	@Test
	void shouldNotifyAboutNewPolicy() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.policyDocumentRevision(1)
			.build();

		PolicyDocument sitePolicy = PolicyDocument.builder()
			.id(policyId)
			.name("policyName")
			.revision(2)
			.build();

		PolicyDocumentExtended policyDocumentExtended = PolicyDocumentExtended.builder()
			.id(policyId)
			.name("policyName")
			.revision(2)
			.build();

		when(userService.getPersistentId(fenixUserId)).thenReturn(id);
		when(userService.getPolicyAcceptances(fenixUserId)).thenReturn(Set.of(policyAcceptance));
		when(policyDocumentRepository.findAllByUserId(eq(fenixUserId), any())).thenReturn(Set.of(policyDocumentExtended));
		when(policyDocumentRepository.findSitePolicy("siteId")).thenReturn(Optional.of(sitePolicy));
		when(policyDocumentRepository.findByUserGrantId("grantId")).thenReturn(Optional.of(PolicyDocument.builder()
			.id(policyId)
			.build()));


		emailNotificationDAO.notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId,"grantId");

		verify(userService).sendUserNotification(id, "policyAcceptanceNew",
			Map.of("custom.name", "policyName",
				"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL)
		);
	}

	@Test
	void shouldNotifyAboutNewSitePolicyAndServicePolicy() {
		PolicyId sitePolicyId = new PolicyId(UUID.randomUUID());
		PolicyId servicePolicyId = new PolicyId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		PersistentId id = new PersistentId(UUID.randomUUID().toString());

		PolicyDocument sitePolicy = PolicyDocument.builder()
			.id(sitePolicyId)
			.name("sitePolicyName")
			.revision(2)
			.build();

		PolicyDocumentExtended sitePolicyExtended = PolicyDocumentExtended.builder()
			.id(sitePolicyId)
			.name("sitePolicyName")
			.revision(2)
			.build();

		PolicyDocumentExtended servicePolicyExtended = PolicyDocumentExtended.builder()
			.id(servicePolicyId)
			.name("servicePolicyName")
			.revision(2)
			.build();

		when(userService.getPersistentId(fenixUserId)).thenReturn(id);
		when(userService.getPolicyAcceptances(fenixUserId)).thenReturn(Set.of());
		when(policyDocumentRepository.findAllByUserId(eq(fenixUserId), any())).thenReturn(Set.of(sitePolicyExtended, servicePolicyExtended));
		when(policyDocumentRepository.findSitePolicy("siteId")).thenReturn(Optional.of(sitePolicy));
		when(policyDocumentRepository.findByUserGrantId("grantId")).thenReturn(Optional.of(PolicyDocument.builder()
			.id(servicePolicyId)
			.build()));


		emailNotificationDAO.notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId,"grantId");

		verify(userService).sendUserNotification(id, "policyAcceptanceNew",
			Map.of("custom.name", "sitePolicyName",
				"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL)
		);
		verify(userService).sendUserNotification(id, "policyAcceptanceNew",
			Map.of("custom.name", "servicePolicyName",
				"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL)
		);
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
		when(policyDocumentRepository.findByUserGrantId("grantId")).thenReturn(Optional.of(PolicyDocument.builder()
			.id(policyId)
			.build()));

		emailNotificationDAO.notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId, "grantId");

		verify(userService, times(0)).sendUserNotification(id, "policyAcceptanceNew", Map.of("custom.name", "policyName", "custom.furmsUrl", FURMS_BASE_URL));
	}

	@Test
	void shouldNotifyAllInstalledUsersAndAttachedUsersInUnityGroupAboutSitePolicyChange() {
		//given
		final FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		final PersistentId id = new PersistentId(UUID.randomUUID().toString());
		final SiteId siteId = new SiteId(UUID.randomUUID().toString());

		final PolicyDocument policyDocument = PolicyDocument.builder()
				.id(new PolicyId(UUID.randomUUID()))
				.revision(1)
				.name("name")
				.siteId(siteId.id)
				.contentType(PolicyContentType.EMBEDDED)
				.wysiwygText("wysiwygText")
				.build();

		when(policyDocumentRepository.findSitePolicy(siteId.id)).thenReturn(Optional.of(policyDocument));
		when(userOperationRepository.findAllUserAdditionsBySiteId(siteId.id)).thenReturn(Set.of(
				UserAddition.builder().userId(fenixUserId.id).build()));
		when(userService.findAllSiteUsers(siteId)).thenReturn(Set.of(fenixUserId));
		when(policyDocumentDAO.getPolicyAcceptances(fenixUserId)).thenReturn(Set.of(
				PolicyAcceptance.builder().policyDocumentId(policyDocument.id).policyDocumentRevision(0).build()));
		when(userService.getPersistentId(fenixUserId)).thenReturn(id);

		//when
		emailNotificationDAO.notifyAllUsersAboutPolicyAssignmentChange(siteId);

		verify(userService, times(1))
				.sendUserNotification(id, "policyAcceptanceNew",
						Map.of("custom.name", "name",
								"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL));
	}

	@Test
	void shouldNotifyAllInstalledUsersAndAttachedUsersInUnityGroupAboutSiteServicePolicyChange() {
		//given
		final FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		final PersistentId id = new PersistentId(UUID.randomUUID().toString());
		final SiteId siteId = new SiteId(UUID.randomUUID().toString());

		final PolicyDocument policyDocument = PolicyDocument.builder()
				.id(new PolicyId(UUID.randomUUID()))
				.revision(1)
				.name("name")
				.siteId(siteId.id)
				.contentType(PolicyContentType.EMBEDDED)
				.wysiwygText("wysiwygText")
				.build();
		final InfraService infraService = InfraService.builder()
				.policyId(policyDocument.id)
				.siteId(siteId.id)
				.build();

		when(policyDocumentRepository.findById(infraService.policyId)).thenReturn(Optional.of(policyDocument));
		when(userOperationRepository.findAllUserAdditionsBySiteId(siteId.id)).thenReturn(Set.of(
				UserAddition.builder().userId(fenixUserId.id).build()));
		when(userService.findAllSiteUsers(siteId)).thenReturn(Set.of(fenixUserId));
		when(policyDocumentDAO.getPolicyAcceptances(fenixUserId)).thenReturn(Set.of(
				PolicyAcceptance.builder().policyDocumentId(policyDocument.id).policyDocumentRevision(0).build()));
		when(userService.getPersistentId(fenixUserId)).thenReturn(id);

		//when
		emailNotificationDAO.notifyAllUsersAboutPolicyAssignmentChange(infraService);

		verify(userService, times(1))
				.sendUserNotification(id, "policyAcceptanceNew",
						Map.of("custom.name", "name",
								"custom.furmsUrl", POLICY_DOCUMENT_BASE_URL));
	}
}