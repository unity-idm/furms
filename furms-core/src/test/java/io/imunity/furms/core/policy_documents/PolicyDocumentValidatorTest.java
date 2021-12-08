/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.PolicyDocumentIsInconsistentException;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyDocumentValidatorTest {

	@Mock
	private PolicyDocumentRepository repository;
	@Mock
	private SiteRepository siteRepository;

	@InjectMocks
	private PolicyDocumentValidator validator;

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
		when(repository.isNamePresent("siteId", "name")).thenReturn(false);

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
			.contentType(PolicyContentType.EMBEDDED)
			.name("name")
			.build();

		PolicyDocument policyDocument1 = PolicyDocument.builder()
			.id(policyId)
			.siteId("siteId")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.EMBEDDED)
			.name("name1")
			.build();

		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument1));


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
		when(repository.isNamePresent("siteId", "name")).thenReturn(true);

		String message = assertThrows(PolicyDocumentIsInconsistentException.class, () -> validator.validateCreate(policyDocument))
			.getMessage();
		assertEquals(message, "PDF content type should not contain wyswige text");
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

		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));


		String message = assertThrows(PolicyDocumentIsInconsistentException.class, () -> validator.validateUpdate(policyDocument))
			.getMessage();
		assertEquals(message, "PDF content type should not contain wyswige text");
	}

	@Test
	void shouldThrowsExceptionWhileValidCreationWhenEmbeddedPolicyHasFile() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId("siteId")
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.EMBEDDED)
			.file(new byte[2], "pdf", "name-rev0")
			.build();

		when(siteRepository.exists("siteId")).thenReturn(true);
		when(repository.isNamePresent("siteId", "name")).thenReturn(true);

		String message = assertThrows(PolicyDocumentIsInconsistentException.class, () -> validator.validateCreate(policyDocument))
			.getMessage();
		assertEquals(message, "Embedded content type should not contain file");
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
		when(repository.isNamePresent("siteId", "name")).thenReturn(true);

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
			.file(new byte[2], "pdf", "name-rev0")
			.build();

		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		String message = assertThrows(PolicyDocumentIsInconsistentException.class, () -> validator.validateUpdate(policyDocument))
			.getMessage();
		assertEquals(message, "Embedded content type should not contain file");
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

		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		validator.validateUpdate(policyDocument);
	}
}