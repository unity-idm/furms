/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentCreateEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentRemovedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptances;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

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
	private NotificationDAO notificationDAO;
	@Mock
	private UserOperationRepository userOperationRepository;

	private PolicyDocumentServiceImpl service;
	private InOrder orderVerifier;


	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new PolicyDocumentServiceImpl(repository, validator, policyDocumentDAO, authzService, notificationDAO, userOperationRepository, publisher);
		orderVerifier = inOrder(repository, validator, publisher, policyDocumentDAO, notificationDAO);
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
	void shouldCreate() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder().build();
		when(repository.create(policyDocument)).thenReturn(policyId);

		service.create(policyDocument);

		orderVerifier.verify(validator).validateCreate(policyDocument);
		orderVerifier.verify(repository).create(policyDocument);
		orderVerifier.verify(publisher).publishEvent(new PolicyDocumentCreateEvent(policyId));
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
		PolicyDocument policyDocument = PolicyDocument.builder().build();
		when(repository.update(policyDocument, true)).thenReturn(policyId);

		service.updateWithRevision(policyDocument);

		orderVerifier.verify(validator).validateUpdate(policyDocument);
		orderVerifier.verify(repository).update(policyDocument, true);
		orderVerifier.verify(notificationDAO).notifyAboutChangedPolicy(policyDocument);
		orderVerifier.verify(publisher).publishEvent(new PolicyDocumentUpdatedEvent(policyId));
	}

	@Test
	void shouldDelete() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		service.delete("siteId", policyId);

		orderVerifier.verify(repository).deleteById(policyId);
		orderVerifier.verify(publisher).publishEvent(new PolicyDocumentRemovedEvent(policyId));
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
	void shouldFindAllAllUserWithoutPolicyAcceptance() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());

		when(repository.findById(policyId)).thenReturn(Optional.of(
			PolicyDocument.builder().build()
			)
		);
		FURMSUser user = FURMSUser.builder()
			.fenixUserId(userId)
			.email("email")
			.build();
		when(policyDocumentDAO.getUserPolicyAcceptances("siteId")).thenReturn(Set.of(
			new UserPolicyAcceptances(user, Set.of())
		));
		when(userOperationRepository.findAllUserAdditionsByUserId("siteId")).thenReturn(Set.of(
			UserAddition.builder()
				.userId(userId.id)
				.status(UserStatus.ADDED)
				.build()
		));

		Set<FURMSUser> users = service.findAllUsersWithoutCurrentRevisionPolicyAcceptance("siteId", policyId);

		orderVerifier.verify(repository).findById(policyId);
		orderVerifier.verify(policyDocumentDAO).getUserPolicyAcceptances("siteId");

		assertEquals(1, users.size());
		assertEquals(user, users.iterator().next());
	}

	@Test
	void shouldNotFindUserWithoutPolicyAcceptanceIfUserIsNotInstalledOnSite() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());

		when(repository.findById(policyId)).thenReturn(Optional.of(
			PolicyDocument.builder().build()
			)
		);
		FURMSUser user = FURMSUser.builder()
			.fenixUserId(userId)
			.email("email")
			.build();
		when(policyDocumentDAO.getUserPolicyAcceptances("siteId")).thenReturn(Set.of(
			new UserPolicyAcceptances(user, Set.of())
		));
		when(userOperationRepository.findAllUserAdditionsByUserId("siteId")).thenReturn(Set.of());

		Set<FURMSUser> users = service.findAllUsersWithoutCurrentRevisionPolicyAcceptance("siteId", policyId);

		orderVerifier.verify(repository).findById(policyId);
		orderVerifier.verify(policyDocumentDAO).getUserPolicyAcceptances("siteId");

		assertEquals(0, users.size());
	}

	@Test
	void shouldFindAllUserWithoutCurrentRevisionPolicyAgreement() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());

		when(repository.findById(policyId)).thenReturn(Optional.of(
			PolicyDocument.builder()
				.id(policyId)
				.revision(2)
				.build()
			)
		);
		FURMSUser user = FURMSUser.builder()
			.fenixUserId(userId)
			.email("email")
			.build();
		when(policyDocumentDAO.getUserPolicyAcceptances("siteId")).thenReturn(Set.of(
			new UserPolicyAcceptances(user, Set.of(PolicyAcceptance.builder()
				.policyDocumentId(policyId)
				.policyDocumentRevision(1)
				.build())
			)
		));
		when(userOperationRepository.findAllUserAdditionsByUserId("siteId")).thenReturn(Set.of(
			UserAddition.builder()
				.userId(userId.id)
				.status(UserStatus.ADDED)
				.build()
		));

		Set<FURMSUser> users = service.findAllUsersWithoutCurrentRevisionPolicyAcceptance("siteId", policyId);

		orderVerifier.verify(repository).findById(policyId);
		orderVerifier.verify(policyDocumentDAO).getUserPolicyAcceptances("siteId");

		assertEquals(1, users.size());
		assertEquals(user, users.iterator().next());
	}

	@Test
	void shouldAddPolicyToCurrentUser() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.build();

		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("email")
			.fenixUserId(userId).build()
		);

		service.addCurrentUserPolicyAcceptance(policyAcceptance);

		orderVerifier.verify(policyDocumentDAO).addUserPolicyAcceptance(userId, policyAcceptance);
	}

	@Test
	void shouldAddPolicyToUser() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.build();

		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.email("email")
			.fenixUserId(userId).build()
		);

		service.addUserPolicyAcceptance("siteId", userId, policyAcceptance);

		orderVerifier.verify(policyDocumentDAO).addUserPolicyAcceptance(userId, policyAcceptance);
	}
}