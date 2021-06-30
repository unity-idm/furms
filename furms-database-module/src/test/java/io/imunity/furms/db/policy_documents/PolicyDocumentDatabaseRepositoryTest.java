/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyFile;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PolicyDocumentDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private PolicyDocumentEntityRepository policyDocumentEntityRepository;
	@Autowired
	private PolicyDocumentDatabaseRepository repository;

	private UUID siteId;

	@BeforeEach
	void init() {
		Site site = Site.builder()
			.name("name")
			.connectionInfo("alala")
			.build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));
	}

	@Test
	void shouldFindById() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		Optional<PolicyDocument> policyDocument = repository.findById(new PolicyId(saved.getId()));

		assertThat(policyDocument).isPresent();

		assertThat(policyDocument.get().siteId).isEqualTo(saved.siteId.toString());
		assertThat(policyDocument.get().name).isEqualTo(saved.name);
		assertThat(policyDocument.get().workflow).isEqualTo(PolicyWorkflow.valueOf(saved.workflow));
		assertThat(policyDocument.get().revision).isEqualTo(saved.revision);
		assertThat(policyDocument.get().contentType).isEqualTo(PolicyContentType.valueOf(saved.contentType));
		assertThat(policyDocument.get().wysiwygText).isEqualTo(saved.wysiwygText);
		assertThat(policyDocument.get().policyFile).isEqualTo(PolicyFile.empty());
	}

	@Test
	void shouldFindAllBySiteId() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity policyDocumentEntity1 = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name2")
			.workflow(1)
			.revision(0)
			.contentType(1)
			.file(new byte[1])
			.fileType("pdf")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);
		PolicyDocumentEntity saved1 = policyDocumentEntityRepository.save(policyDocumentEntity1);

		Set<PolicyDocument> policyDocuments = repository.findAllBySiteId(siteId.toString());

		assertThat(policyDocuments.size()).isEqualTo(2);
	}

	@Test
	void shouldCreateTextPolicyDocument() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId(siteId.toString())
			.name("name")
			.workflow(PolicyWorkflow.PAPER_BASED)
			.revision(0)
			.contentType(PolicyContentType.EMBEDDED)
			.wysiwygText("sdsadas")
			.file(PolicyFile.empty())
			.build();

		PolicyId policyId = repository.create(policyDocument);

		Optional<PolicyDocumentEntity> policyDocumentEntity = policyDocumentEntityRepository.findById(policyId.id);

		assertThat(policyDocumentEntity).isPresent();

		assertThat(policyDocumentEntity.get().siteId.toString()).isEqualTo(policyDocument.siteId);
		assertThat(policyDocumentEntity.get().name).isEqualTo(policyDocument.name);
		assertThat(policyDocumentEntity.get().workflow).isEqualTo(policyDocument.workflow.getPersistentId());
		assertThat(policyDocumentEntity.get().revision).isEqualTo(policyDocument.revision);
		assertThat(policyDocumentEntity.get().contentType).isEqualTo(policyDocument.contentType.getPersistentId());
		assertThat(policyDocumentEntity.get().wysiwygText).isEqualTo(policyDocument.wysiwygText);
		assertThat(policyDocumentEntity.get().file).isEqualTo(null);
		assertThat(policyDocumentEntity.get().fileType).isEqualTo(null);
	}

	@Test
	void shouldCreateFilePolicyDocument() {
		PolicyDocument policyDocument = PolicyDocument.builder()
			.siteId(siteId.toString())
			.name("name")
			.workflow(PolicyWorkflow.PAPER_BASED)
			.revision(0)
			.contentType(PolicyContentType.PDF)
			.wysiwygText(" ")
			.file(new byte[1], "pdf")
			.build();

		PolicyId policyId = repository.create(policyDocument);

		Optional<PolicyDocumentEntity> policyDocumentEntity = policyDocumentEntityRepository.findById(policyId.id);

		assertThat(policyDocumentEntity).isPresent();

		assertThat(policyDocumentEntity.get().siteId.toString()).isEqualTo(policyDocument.siteId);
		assertThat(policyDocumentEntity.get().name).isEqualTo(policyDocument.name);
		assertThat(policyDocumentEntity.get().workflow).isEqualTo(policyDocument.workflow.getPersistentId());
		assertThat(policyDocumentEntity.get().revision).isEqualTo(policyDocument.revision);
		assertThat(policyDocumentEntity.get().contentType).isEqualTo(policyDocument.contentType.getPersistentId());
		assertThat(policyDocumentEntity.get().wysiwygText).isEqualTo(null);
		assertThat(policyDocumentEntity.get().file).isEqualTo(new byte[1]);
		assertThat(policyDocumentEntity.get().fileType).isEqualTo("pdf");
	}

	@Test
	void shouldUpdateWithRevision() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(new PolicyId(saved.getId()))
			.siteId(siteId.toString())
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.revision(0)
			.contentType(PolicyContentType.PDF)
			.wysiwygText(" ")
			.file(new byte[1], "pdf")
			.build();

		repository.update(policyDocument, true);


		Optional<PolicyDocumentEntity> readPolicyDocumentEntity = policyDocumentEntityRepository.findById(saved.getId());
		assertThat(readPolicyDocumentEntity).isPresent();

		assertThat(readPolicyDocumentEntity.get().siteId.toString()).isEqualTo(policyDocument.siteId);
		assertThat(readPolicyDocumentEntity.get().name).isEqualTo(policyDocument.name);
		assertThat(readPolicyDocumentEntity.get().workflow).isEqualTo(policyDocumentEntity.workflow);
		assertThat(readPolicyDocumentEntity.get().revision).isEqualTo(1);
		assertThat(readPolicyDocumentEntity.get().contentType).isEqualTo(policyDocument.contentType.getPersistentId());
		assertThat(readPolicyDocumentEntity.get().wysiwygText).isEqualTo(null);
		assertThat(readPolicyDocumentEntity.get().file).isEqualTo(new byte[1]);
		assertThat(readPolicyDocumentEntity.get().fileType).isEqualTo("pdf");
	}

	@Test
	void shouldUpdateWithoutRevision() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(new PolicyId(saved.getId()))
			.siteId(siteId.toString())
			.name("name")
			.workflow(PolicyWorkflow.WEB_BASED)
			.revision(0)
			.contentType(PolicyContentType.PDF)
			.wysiwygText(" ")
			.file(new byte[1], "pdf")
			.build();

		repository.update(policyDocument, false);


		Optional<PolicyDocumentEntity> readPolicyDocumentEntity = policyDocumentEntityRepository.findById(saved.getId());
		assertThat(readPolicyDocumentEntity).isPresent();

		assertThat(readPolicyDocumentEntity.get().siteId.toString()).isEqualTo(policyDocument.siteId);
		assertThat(readPolicyDocumentEntity.get().name).isEqualTo(policyDocument.name);
		assertThat(readPolicyDocumentEntity.get().workflow).isEqualTo(policyDocumentEntity.workflow);
		assertThat(readPolicyDocumentEntity.get().revision).isEqualTo(0);
		assertThat(readPolicyDocumentEntity.get().contentType).isEqualTo(policyDocument.contentType.getPersistentId());
		assertThat(readPolicyDocumentEntity.get().wysiwygText).isEqualTo(null);
		assertThat(readPolicyDocumentEntity.get().file).isEqualTo(new byte[1]);
		assertThat(readPolicyDocumentEntity.get().fileType).isEqualTo("pdf");
	}

	@Test
	void shouldDeleteById() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		repository.deleteById(new PolicyId(saved.getId()));

		Optional<PolicyDocumentEntity> readPolicyDocumentEntity = policyDocumentEntityRepository.findById(saved.getId());
		assertThat(readPolicyDocumentEntity).isEmpty();
	}

	@Test
	void shouldNameBePresent() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(repository.isNamePresent(siteId.toString(), "name2")).isTrue();
	}

	@Test
	void shouldNameNotBePresent() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(repository.isNamePresent(siteId.toString(), "name")).isFalse();
	}
}