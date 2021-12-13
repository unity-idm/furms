/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.policy_documents.AssignedPolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentCreatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentRemovedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserAcceptedPolicyEvent;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyDocumentServiceImplTest {

	@Mock
	private PolicyDocumentRepository repository;
	@Mock
	private PolicyDocumentValidator validator;
	@Mock
	private PolicyDocumentDAO policyDocumentDAO;
	@Mock
	private AuthzService authzService;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private PolicyNotificationService policyNotificationService;
	@Mock
	private SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private UserOperationRepository userRepository;


	private PolicyDocumentServiceImpl service;
	private InOrder orderVerifier;


	@BeforeEach
	void init() {
		service = new PolicyDocumentServiceImpl(
			authzService, repository, validator, policyDocumentDAO, policyNotificationService,
			siteAgentPolicyDocumentService, siteRepository,
			userRepository, usersDAO, publisher
		);
		orderVerifier = inOrder(repository, validator, publisher, policyDocumentDAO, policyNotificationService, publisher);
	}

	@Test
	void shouldFindById() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		service.findById("siteId", policyId);

		orderVerifier.verify(repository).findById(policyId);
	}

	@Test
	void shouldFindAllBySiteId() {
		service.findAllBySiteId("siteId");

		orderVerifier.verify(repository).findAllBySiteId("siteId");
	}

	@Test
	void shouldResendPolicyInfo() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PersistentId persistentId = new PersistentId("id");
		PolicyDocument policyDocument = PolicyDocument.builder().build();

		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		service.resendPolicyInfo("siteId", persistentId, policyId);

		orderVerifier.verify(policyNotificationService).notifyUserAboutNewPolicy(persistentId, policyDocument);
	}

	@Test
	void shouldCreate() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder().build();
		when(repository.create(policyDocument)).thenReturn(policyId);

		service.create(policyDocument);

		orderVerifier.verify(validator).validateCreate(policyDocument);
		orderVerifier.verify(repository).create(policyDocument);
		orderVerifier.verify(publisher).publishEvent(new PolicyDocumentCreatedEvent(policyId));
	}

	@Test
	void shouldUpdate() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder().build();
		when(repository.update(policyDocument, false)).thenReturn(policyId);

		service.update(policyDocument);

		orderVerifier.verify(validator).validateUpdate(policyDocument);
		orderVerifier.verify(repository).update(policyDocument, false);
		orderVerifier.verify(publisher).publishEvent(new PolicyDocumentUpdatedEvent(policyId));

	}

	@Test
	void shouldUpdateWithRevision() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId("siteId")
			.build();
		Site site = Site.builder()
			.build();

		when(repository.update(policyDocument, true)).thenReturn(policyId);
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findById("siteId")).thenReturn(Optional.of(site));

		service.updateWithRevision(policyDocument);

		orderVerifier.verify(validator).validateUpdate(policyDocument);
		orderVerifier.verify(repository).update(policyDocument, true);
		orderVerifier.verify(policyNotificationService).notifyAboutChangedPolicy(policyDocument);
		orderVerifier.verify(publisher).publishEvent(new PolicyDocumentUpdatedEvent(policyId));
	}

	@Test
	void shouldSendUpdateSitePolicyDocument() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		SiteExternalId externalId = new SiteExternalId("id");
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId("siteId")
			.build();
		Site site = Site.builder()
			.policyId(policyId)
			.externalId(externalId)
			.build();

		when(repository.update(policyDocument, true)).thenReturn(policyId);
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findById("siteId")).thenReturn(Optional.of(site));

		service.updateWithRevision(policyDocument);

		Mockito.verify(siteAgentPolicyDocumentService).updatePolicyDocument(externalId, policyDocument);
	}

	@Test
	void shouldSendUpdateServicePolicyDocument() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		SiteExternalId externalId = new SiteExternalId("id");
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId("siteId")
			.build();
		Site site = Site.builder()
			.externalId(externalId)
			.build();
		AssignedPolicyDocument servicePolicyDocument = AssignedPolicyDocument.builder()
			.id(policyId)
			.serviceId("serviceId")
			.build();

		when(repository.update(policyDocument, true)).thenReturn(policyId);
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(repository.findAllAssignPoliciesBySiteId("siteId")).thenReturn(Set.of(servicePolicyDocument));
		when(siteRepository.findById("siteId")).thenReturn(Optional.of(site));

		service.updateWithRevision(policyDocument);

		Mockito.verify(siteAgentPolicyDocumentService).updatePolicyDocument(externalId, policyDocument, "serviceId");
	}

	@Test
	void shouldDelete() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		service.delete("siteId", policyId);

		orderVerifier.verify(repository).deleteById(policyId);
		orderVerifier.verify(publisher).publishEvent(new PolicyDocumentRemovedEvent(policyId));
	}

	@Test
	void shouldFindAllUsersPolicyAcceptances() {
		String siteId = "siteId";
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("id");
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.build();
		FURMSUser firstUser = FURMSUser.builder()
			.fenixUserId(fenixUserId)
			.email("email")
			.build();
		FURMSUser secondUser = FURMSUser.builder()
			.fenixUserId(new FenixUserId("secondUserId"))
			.email("email")
			.build();
		UserPolicyAcceptances firstUserPolicyAcceptances = new UserPolicyAcceptances(firstUser, Set.of(policyAcceptance));
		UserPolicyAcceptances secondUserPolicyAcceptances = new UserPolicyAcceptances(secondUser, Set.of(policyAcceptance));

		when(repository.findAllPolicyUsers(siteId, policyId)).thenReturn(Set.of(fenixUserId));
		when(policyDocumentDAO.getUserPolicyAcceptances(siteId)).thenReturn(Set.of(firstUserPolicyAcceptances, secondUserPolicyAcceptances));

		Set<UserPolicyAcceptances> usersPolicyAcceptances = service.findAllUsersPolicyAcceptances(policyId, "siteId");

		assertEquals(1, usersPolicyAcceptances.size());
		assertEquals(firstUserPolicyAcceptances, usersPolicyAcceptances.iterator().next());

	}

	@Test
	void shouldFindAllByCurrentUser() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId0 = new PolicyId(UUID.randomUUID());
		PolicyId policyId1 = new PolicyId(UUID.randomUUID());
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("email")
			.fenixUserId(userId).build()
		);
		when(policyDocumentDAO.getPolicyAcceptances(userId)).thenReturn(Set.of(
			PolicyAcceptance.builder()
				.policyDocumentId(policyId0)
				.build(),
			PolicyAcceptance.builder()
				.policyDocumentId(policyId1)
				.build()
		));

		service.findAllByCurrentUser();

		orderVerifier.verify(repository).findAllByUserId(eq(userId), any());
	}

	@Test
	void shouldAddPolicyToUser() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.policyDocumentRevision(1)
			.build();
		FURMSUser furmsUser = FURMSUser.builder()
			.email("email")
			.fenixUserId(userId)
			.build();
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id((policyId))
			.siteId("siteId")
			.revision(1)
			.build();


		when(usersDAO.findById(userId)).thenReturn(Optional.of(furmsUser));
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		service.addUserPolicyAcceptance("siteId", userId, policyAcceptance);

		orderVerifier.verify(policyDocumentDAO).addUserPolicyAcceptance(userId, policyAcceptance);
		orderVerifier.verify(publisher).publishEvent(new UserAcceptedPolicyEvent(userId, policyAcceptance));
		verify(repository, times(0)).findById(PolicyId.empty());
	}

	@Test
	void shouldSendUserPolicyAcceptance() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.revision(1)
			.siteId("siteId")
			.build();
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.policyDocumentRevision(1)
			.build();
		FURMSUser user = FURMSUser.builder()
			.email("email")
			.fenixUserId(userId).build();

		when(usersDAO.findById(userId)).thenReturn(Optional.of(user));
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		service.addUserPolicyAcceptance("siteId", userId, policyAcceptance);

		orderVerifier.verify(policyDocumentDAO).addUserPolicyAcceptance(userId, policyAcceptance);
	}

	@Test
	void shouldAddPolicyToUserWithCurrentRevision() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.revision(1)
			.siteId("siteId")
			.build();
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.build();
		FURMSUser user = FURMSUser.builder()
			.email("email")
			.fenixUserId(userId).build();

		when(usersDAO.findById(userId)).thenReturn(Optional.of(user));
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		service.addUserPolicyAcceptance("siteId", userId, policyAcceptance);

		policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.policyDocumentRevision(1)
			.build();
		orderVerifier.verify(policyDocumentDAO).addUserPolicyAcceptance(userId, policyAcceptance);
	}

	@Test
	void shouldUpdateUsersPolicyAcceptances() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.policyDocumentRevision(1)
			.build();
		FURMSUser furmsUser = FURMSUser.builder()
			.email("email")
			.fenixUserId(userId)
			.build();
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id((policyId))
			.siteId("siteId")
			.revision(1)
			.build();
		Site site = Site.builder()
			.id("siteId")
			.externalId(new SiteExternalId("id"))
			.build();

		AssignedPolicyDocument servicePolicyDocument = AssignedPolicyDocument.builder().build();

		when(usersDAO.findById(userId)).thenReturn(Optional.of(furmsUser));
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(repository.findAllAssignPoliciesBySiteId("siteId")).thenReturn(Set.of(servicePolicyDocument));
		when(siteRepository.findById("siteId")).thenReturn(Optional.of(site));
		when(userRepository.isUserInstalledOnSite(userId, "siteId")).thenReturn(true);
		when(policyDocumentDAO.getPolicyAcceptances(userId)).thenReturn(Set.of(policyAcceptance));

		service.addUserPolicyAcceptance("siteId", userId, policyAcceptance);

		Mockito.verify(siteAgentPolicyDocumentService).updateUsersPolicyAcceptances(site.getExternalId(), new UserPolicyAcceptancesWithServicePolicies(
			furmsUser,
			Set.of(policyAcceptance),
			Optional.empty(),
			Set.of(servicePolicyDocument)
		));
	}
}