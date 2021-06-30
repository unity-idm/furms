/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@Repository
class PolicyDocumentDatabaseRepository implements PolicyDocumentRepository {
	private final PolicyDocumentEntityRepository repository;

	PolicyDocumentDatabaseRepository(PolicyDocumentEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<PolicyDocument> findById(PolicyId policyId) {
		return repository.findById(policyId.id)
			.map(PolicyDocumentEntity::toPolicyDocument);
	}

	@Override
	public Set<PolicyDocument> findAllBySiteId(String siteId) {
		return repository.findAllBySiteId(UUID.fromString(siteId)).stream()
			.map(PolicyDocumentEntity::toPolicyDocument)
			.collect(toSet());
	}

	@Override
	public PolicyId create(PolicyDocument policyDocument) {
		PolicyDocumentEntity savedProjectAllocation = repository.save(
			PolicyDocumentEntity.builder()
				.siteId(UUID.fromString(policyDocument.siteId))
				.name(policyDocument.name)
				.workflow(policyDocument.workflow.getPersistentId())
				.contentType(policyDocument.contentType.getPersistentId())
				.wysiwygText(policyDocument.wysiwygText.isEmpty() ? null : policyDocument.wysiwygText)
				.file(policyDocument.policyFile.getFile().length == 0 ? null : policyDocument.policyFile.getFile())
				.fileType(policyDocument.policyFile.getType())
				.build()
		);
		return new PolicyId(savedProjectAllocation.getId());
	}

	@Override
	public PolicyId update(PolicyDocument policyDocument, boolean revision) {
		return repository.findById(policyDocument.id.id)
			.map(old -> PolicyDocumentEntity.builder()
				.id(old.getId())
				.siteId(old.siteId)
				.name(policyDocument.name)
				.workflow(old.workflow)
				.contentType(old.contentType)
				.revision(revision ? old.revision + 1 : old.revision)
				.wysiwygText(policyDocument.wysiwygText.isEmpty() ? null : policyDocument.wysiwygText)
				.file(policyDocument.policyFile.getFile().length == 0 ? null : policyDocument.policyFile.getFile())
				.fileType(policyDocument.policyFile.getType())
				.build()
			)
			.map(repository::save)
			.map(PolicyDocumentEntity::getId)
			.map(PolicyId::new)
			.orElseThrow(() -> new IllegalStateException("Policy document not found: " + policyDocument.id));
	}

	@Override
	public boolean isNamePresent(String siteId, String name) {
		return !repository.existsBySiteIdAndName(UUID.fromString(siteId), name);
	}

	@Override
	public void deleteById(PolicyId policyId) {
		repository.deleteById(policyId.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}

