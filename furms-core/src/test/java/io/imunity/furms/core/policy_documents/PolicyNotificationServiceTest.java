/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

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
import io.imunity.furms.spi.notifications.EmailNotificationSender;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PolicyNotificationServiceTest {
	@Mock
	private PolicyDocumentDAO policyDocumentDAO;
	@Mock
	private PolicyDocumentRepository policyDocumentRepository;
	@Mock
	private UserOperationRepository userOperationRepository;
	@Mock
	private EmailNotificationSender emailNotificationSender;
	@Mock
	private SiteGroupDAO siteGroupDAO;
	@Mock
	private ApplicationEventPublisher publisher;

	@InjectMocks
	private PolicyNotificationService policyNotificationService;

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

		policyNotificationService.notifyAboutChangedPolicy(policyDocument);

		verify(emailNotificationSender).notifyAboutChangedPolicy(id, "policyName");
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

		policyNotificationService.notifyAboutChangedPolicy(policyDocument);

		verify(emailNotificationSender, times(0)).notifyAboutChangedPolicy(id, "policyName");
	}

	@Test
	void shouldNotifyAboutNewPolicy() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");

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

		when(policyDocumentRepository.findAllByUserId(eq(fenixUserId), any())).thenReturn(Set.of(policyDocumentExtended));
		when(policyDocumentRepository.findSitePolicy("siteId")).thenReturn(Optional.of(sitePolicy));
		when(policyDocumentRepository.findByUserGrantId("grantId")).thenReturn(Optional.of(PolicyDocument.builder()
			.id(policyId)
			.build()));

		policyNotificationService.notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId,"grantId");

		verify(emailNotificationSender).notifyAboutNotAcceptedPolicy(fenixUserId, "policyName");
	}

	@Test
	void shouldNotifyAboutNewSitePolicyAndServicePolicy() {
		PolicyId sitePolicyId = new PolicyId(UUID.randomUUID());
		PolicyId servicePolicyId = new PolicyId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");

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

		when(policyDocumentRepository.findAllByUserId(eq(fenixUserId), any())).thenReturn(Set.of(sitePolicyExtended, servicePolicyExtended));
		when(policyDocumentRepository.findSitePolicy("siteId")).thenReturn(Optional.of(sitePolicy));
		when(policyDocumentRepository.findByUserGrantId("grantId")).thenReturn(Optional.of(PolicyDocument.builder()
			.id(servicePolicyId)
			.build()));


		policyNotificationService.notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId,"grantId");

		verify(emailNotificationSender).notifyAboutNotAcceptedPolicy(fenixUserId, "sitePolicyName");
		verify(emailNotificationSender).notifyAboutNotAcceptedPolicy(fenixUserId, "servicePolicyName");
	}

	@Test
	void shouldNotNotifyAboutNewPolicy() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");

		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.policyDocumentRevision(1)
			.build();

		PolicyDocumentExtended policyDocumentExtended = PolicyDocumentExtended.builder()
			.id(policyId)
			.name("policyName")
			.revision(1)
			.build();

		when(policyDocumentDAO.getPolicyAcceptances(fenixUserId)).thenReturn(Set.of(policyAcceptance));
		when(policyDocumentRepository.findAllByUserId(eq(fenixUserId), any())).thenReturn(Set.of(policyDocumentExtended));
		when(policyDocumentRepository.findByUserGrantId("grantId")).thenReturn(Optional.of(PolicyDocument.builder()
			.id(policyId)
			.build()));

		policyNotificationService.notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId, "grantId");

		verify(emailNotificationSender, times(0)).notifyAboutNotAcceptedPolicy(fenixUserId, "policyName");
	}

	@Test
	void shouldNotifyAllInstalledUsersAndAttachedUsersInUnityGroupAboutSitePolicyChange() {
		//given
		final FenixUserId fenixUserId = new FenixUserId("fenixUserId");
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
		when(policyDocumentDAO.getPolicyAcceptances(fenixUserId)).thenReturn(Set.of(
				PolicyAcceptance.builder().policyDocumentId(policyDocument.id).policyDocumentRevision(0).build()));

		//when
		policyNotificationService.notifyAllUsersAboutPolicyAssignmentChange(siteId);

		verify(emailNotificationSender).notifySiteUserAboutPolicyAssignmentChange(fenixUserId, "name");
	}

	@Test
	void shouldNotifyAllInstalledUsersAndAttachedUsersInUnityGroupAboutSiteServicePolicyChange() {
		//given
		final FenixUserId fenixUserId = new FenixUserId("fenixUserId");
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
		when(policyDocumentDAO.getPolicyAcceptances(fenixUserId)).thenReturn(Set.of(
				PolicyAcceptance.builder().policyDocumentId(policyDocument.id).policyDocumentRevision(0).build()));

		//when
		policyNotificationService.notifyAllUsersAboutPolicyAssignmentChange(infraService);

		verify(emailNotificationSender).notifySiteUserAboutPolicyAssignmentChange(fenixUserId, "name");
	}
}