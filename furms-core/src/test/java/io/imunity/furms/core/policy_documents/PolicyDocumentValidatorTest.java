/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.PolicyDocumentIsNotConsistException;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class PolicyDocumentValidatorTest {

	@Mock
	private PolicyDocumentRepository repository;
	@Mock
	private SiteRepository siteRepository;

	private PolicyDocumentValidator validator;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		validator = new PolicyDocumentValidator(repository, siteRepository);
	}

	@Test
	void shouldThrowsExceptionWhileValidCreationWhenPolicyDocumentIsNull() {
		String message = assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(null))
			.getMessage();
		assertEquals(message, "Policy document object cannot be null.");
	}

	@Test
	void shouldThrowsExceptionWhileValidUpdateWhenPolicyDocumentIsNull() {
		String message = assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(null))
			.getMessage();
		assertEquals(message, "Policy document object cannot be null.");
	}

	@Test
	void shouldThrowsExceptionWhileValidCreationWhenSiteNotExist() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId("siteId")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(false);

		String message = assertThrows(IdNotFoundValidationError.class, () -> validator.validateCreate(policyDocument))
			.getMessage();
		assertEquals(message, "Site with declared ID does not exist.");
	}

	@Test
	void shouldThrowsExceptionWhileValidCreationWhenNameIsNotPresent() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId("siteId")
			.name("name")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(true);
		when(repository.isNamePresent("siteId", "name")).thenReturn(true);

		String message = assertThrows(DuplicatedNameValidationError.class, () -> validator.validateCreate(policyDocument))
			.getMessage();
		assertEquals(message, "Policy Document name has to be unique.");
	}

	@Test
	void shouldThrowsExceptionWhileValidUpdateWhenNameIsNotPresent() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());

		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId("siteId")
			.workflow(PolicyWorkflow.WEB_BASED)
			.name("name")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(true);
		when(repository.isNamePresent("siteId", "name")).thenReturn(true);
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));


		String message = assertThrows(DuplicatedNameValidationError.class, () -> validator.validateUpdate(policyDocument))
			.getMessage();
		assertEquals(message, "Policy Document name has to be unique.");
	}

	@Test
	void shouldThrowsExceptionWhileValidCreationWhenPdfPolicyHasText() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId("siteId")
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.PDF)
			.wysiwygText("sdsd")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(true);
		when(repository.isNamePresent("siteId", "name")).thenReturn(false);

		String message = assertThrows(PolicyDocumentIsNotConsistException.class, () -> validator.validateCreate(policyDocument))
			.getMessage();
		assertEquals(message, "PDF content type should not contains wyswige text");
	}

	@Test
	void shouldThrowsExceptionWhileValidUpdateWhenPdfPolicyHasText() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());

		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId("siteId")
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.PDF)
			.wysiwygText("sdsd")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(true);
		when(repository.isNamePresent("siteId", "name")).thenReturn(false);
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));


		String message = assertThrows(PolicyDocumentIsNotConsistException.class, () -> validator.validateUpdate(policyDocument))
			.getMessage();
		assertEquals(message, "PDF content type should not contains wyswige text");
	}

	@Test
	void shouldThrowsExceptionWhileValidCreationWhenEmbeddedPolicyHasFile() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId("siteId")
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.EMBEDDED)
			.file(new byte[2], "pdf")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(true);
		when(repository.isNamePresent("siteId", "name")).thenReturn(false);

		String message = assertThrows(PolicyDocumentIsNotConsistException.class, () -> validator.validateCreate(policyDocument))
			.getMessage();
		assertEquals(message, "Embedded content type should not contains file");
	}

	@Test
	void shouldPassCreate() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId("siteId")
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.EMBEDDED)
			.wysiwygText("dsds")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(true);
		when(repository.isNamePresent("siteId", "name")).thenReturn(false);

		validator.validateCreate(policyDocument);
	}

	@Test
	void shouldThrowsExceptionWhileValidUpdateWhenEmbeddedPolicyHasFile() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId("siteId")
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.EMBEDDED)
			.file(new byte[2], "pdf")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(true);
		when(repository.isNamePresent("siteId", "name")).thenReturn(false);
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		String message = assertThrows(PolicyDocumentIsNotConsistException.class, () -> validator.validateUpdate(policyDocument))
			.getMessage();
		assertEquals(message, "Embedded content type should not contains file");
	}

	@Test
	void shouldPassUpdate() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId("siteId")
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.EMBEDDED)
			.wysiwygText("pdfdsd")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(true);
		when(repository.isNamePresent("siteId", "name")).thenReturn(false);
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		validator.validateUpdate(policyDocument);
	}
}