/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyDocumentExtended;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.domain.policy_documents.ServicePolicyDocument;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Stream;

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
	public Set<PolicyDocumentExtended> findAllByUserId(FenixUserId userId, BiFunction<PolicyId, Integer, LocalDateTime> acceptedGetter) {
		return Stream.of(
			repository.findAllSitePoliciesByUserId(userId.id),
			repository.findAllServicePoliciesByUserId(userId.id)
		)
			.flatMap(Collection::stream)
			.map(policyDocument -> {
				PolicyId id = new PolicyId(policyDocument.getId());
					return PolicyDocumentExtended.builder()
						.id(id)
						.siteId(policyDocument.siteId.toString())
						.siteName(policyDocument.siteName)
						.serviceName(policyDocument.serviceName)
						.acceptedTime(acceptedGetter.apply(id, policyDocument.revision))
						.name(policyDocument.name)
						.workflow(PolicyWorkflow.valueOf(policyDocument.workflow))
						.revision(policyDocument.revision)
						.contentType(PolicyContentType.valueOf(policyDocument.contentType))
						.wysiwygText(policyDocument.htmlText)
						.file(policyDocument.file, policyDocument.fileType, policyDocument.name + "-rev" + policyDocument.revision)
						.build();
				}
			)
			.collect(toSet());
	}

	@Override
	public Set<PolicyDocument> findAllBySiteId(String siteId) {
		return repository.findAllBySiteId(UUID.fromString(siteId)).stream()
			.map(PolicyDocumentEntity::toPolicyDocument)
			.collect(toSet());
	}

	@Override
	public Set<ServicePolicyDocument> findAllAssignPoliciesBySiteId(String siteId) {
		return repository.findAllServicePoliciesBySiteId(UUID.fromString(siteId));
	}

	@Override
	public Set<PolicyDocument> findAllSitePoliciesByUserId(PersistentId userId) {
		return repository.findAllSitePoliciesByUserId(userId.id).stream()
				.map(PolicyDocumentExtendedEntity::toPolicyDocument)
				.collect(toSet());
	}

	@Override
	public Set<PolicyDocument> findAllServicePoliciesByUserId(PersistentId userId) {
		return repository.findAllServicePoliciesByUserId(userId.id).stream()
				.map(PolicyDocumentExtendedEntity::toPolicyDocument)
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
				.wysiwygText(policyDocument.htmlText.isBlank() ? null : policyDocument.htmlText)
				.file(policyDocument.policyFile.getFile().length == 0 ? null : policyDocument.policyFile.getFile())
				.fileType(policyDocument.policyFile.getTypeExtension())
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
				.contentType(policyDocument.contentType.getPersistentId())
				.revision(revision ? old.revision + 1 : old.revision)
				.wysiwygText(policyDocument.htmlText.isBlank() ? null : policyDocument.htmlText)
				.file(policyDocument.policyFile.getFile().length == 0 ? null : policyDocument.policyFile.getFile())
				.fileType(policyDocument.policyFile.getTypeExtension())
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

