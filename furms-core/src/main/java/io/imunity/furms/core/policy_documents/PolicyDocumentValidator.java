/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.PolicyDocumentIsInconsistentException;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

@Component
class PolicyDocumentValidator {

	private final PolicyDocumentRepository repository;
	private final SiteRepository siteRepository;

	PolicyDocumentValidator(PolicyDocumentRepository repository, SiteRepository siteRepository) {
		this.repository = repository;
		this.siteRepository = siteRepository;
	}

	void validateCreate(PolicyDocument policyDocument) {
		notNull(policyDocument, "Policy document object cannot be null.");

		assertSiteIdExists(policyDocument);
		assertNameIsPresent(policyDocument);

		notNull(policyDocument.workflow, "Workflow cannot be null.");
		notNull(policyDocument.contentType, "Content type cannot be null.");

		assertPolicyDocumentIsConsist(policyDocument);
	}

	void validateUpdate(PolicyDocument policyDocument) {
		notNull(policyDocument, "Policy document object cannot be null.");
		Optional<PolicyDocument> savedPolicyDocument = repository.findById(policyDocument.id);

		assertPolicyDocumentExists(savedPolicyDocument);
		assertSiteIdIsNotChanged(policyDocument, savedPolicyDocument.get());
		assertWorkflowIsNotChanged(policyDocument, savedPolicyDocument.get());

		assertNameIsSameOrPresent(policyDocument, savedPolicyDocument.get());

		notNull(policyDocument.contentType, "Content type cannot be null.");

		assertPolicyDocumentIsConsist(policyDocument);
	}

	private void assertPolicyDocumentIsConsist(PolicyDocument policyDocument) {
		if(policyDocument.contentType.equals(PolicyContentType.PDF) && policyDocument.htmlText != null && !policyDocument.htmlText.isBlank())
			throw new PolicyDocumentIsInconsistentException("PDF content type should not contain wyswige text");
		if(policyDocument.contentType.equals(PolicyContentType.EMBEDDED) && !policyDocument.policyFile.isEmpty()) {
			throw new PolicyDocumentIsInconsistentException("Embedded content type should not contain file");
		}
		if(policyDocument.htmlText == null || policyDocument.htmlText.isBlank() && policyDocument.policyFile.isEmpty()) {
			throw new PolicyDocumentIsInconsistentException("Html text and policy file are nulls");
		}
	}

	private void assertNameIsPresent(PolicyDocument policyDocument) {
		notNull(policyDocument.name, "Policy document object cannot be null.");
		validateName(policyDocument.siteId, policyDocument.name);
	}

	private void assertNameIsSameOrPresent(PolicyDocument policyDocument, PolicyDocument savedPolicyDocument) {
		notNull(policyDocument.name, "Policy document object cannot be null.");
		if(policyDocument.name.equals(savedPolicyDocument.name))
			return;
		validateName(policyDocument.siteId, policyDocument.name);
	}

	private void assertSiteIdExists(PolicyDocument policyDocument) {
		notNull(policyDocument.siteId, "Policy document object cannot be null.");
		if (!siteRepository.exists(policyDocument.siteId))
			throw new IdNotFoundValidationError("Site with declared ID does not exist.");
	}

	private void assertSiteIdIsNotChanged(PolicyDocument policyDocument, PolicyDocument savedPolicyDocument) {
		if(!savedPolicyDocument.siteId.equals(policyDocument.siteId))
			throw new IllegalArgumentException("Site change is forbidden");
	}

	private void assertWorkflowIsNotChanged(PolicyDocument policyDocument, PolicyDocument savedPolicyDocument) {
		if(!savedPolicyDocument.workflow.equals(policyDocument.workflow))
			throw new IllegalArgumentException("Workflow change is forbidden");
	}

	private void assertPolicyDocumentExists(Optional<PolicyDocument> savedPolicyDocument) {
		if(savedPolicyDocument.isEmpty())
			throw new IdNotFoundValidationError("PolicyDocument with declared ID does not exist.");
	}

	private void validateName(SiteId siteId, String name) {
		notNull(name, "Site name has to be declared.");
		assertTrue(repository.isNamePresent(siteId, name), () -> new DuplicatedNameValidationError("Policy Document name has to be unique."));
	}
}
