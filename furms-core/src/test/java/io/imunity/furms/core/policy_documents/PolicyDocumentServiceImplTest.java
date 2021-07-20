/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyDocumentCreateEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyDocumentRemovedEvent;
import io.imunity.furms.domain.policy_documents.PolicyDocumentUpdatedEvent;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

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
	private ApplicationEventPublisher publisher;

	private PolicyDocumentServiceImpl service;
	private InOrder orderVerifier;


	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new PolicyDocumentServiceImpl(repository, validator, policyDocumentDAO, publisher);
		orderVerifier = inOrder(repository, validator, publisher);
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
		orderVerifier.verify(publisher).publishEvent(new PolicyDocumentUpdatedEvent(policyId));
	}

	@Test
	void shouldDelete() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		service.delete("siteId", policyId);

		orderVerifier.verify(repository).deleteById(policyId);
		orderVerifier.verify(publisher).publishEvent(new PolicyDocumentRemovedEvent(policyId));
	}
}