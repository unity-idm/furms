/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PolicyDocumentEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private PolicyDocumentEntityRepository policyDocumentEntityRepository;

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
	void shouldFindAllBySiteId() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		Set<PolicyDocumentEntity> policyDocumentEntities = policyDocumentEntityRepository.findAllBySiteId(siteId);

		assertThat(policyDocumentEntities.size()).isEqualTo(1);
		PolicyDocumentEntity next = policyDocumentEntities.iterator().next();

		assertThat(saved.siteId).isEqualTo(next.siteId);
		assertThat(saved.name).isEqualTo(next.name);
		assertThat(saved.workflow).isEqualTo(next.workflow);
		assertThat(saved.revision).isEqualTo(next.revision);
		assertThat(saved.contentType).isEqualTo(next.contentType);
		assertThat(saved.htmlText).isEqualTo(next.htmlText);
		assertThat(saved.file).isEqualTo(next.file);
		assertThat(saved.fileType).isEqualTo(next.fileType);
	}

	@Test
	void shouldExistBySiteIdAndName() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(policyDocumentEntityRepository.existsBySiteIdAndName(siteId, "name")).isTrue();
	}

	@Test
	void shouldNotExistBySiteIdAndName() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		assertThat(policyDocumentEntityRepository.existsBySiteIdAndName(UUID.randomUUID(), "name")).isFalse();
	}

	@Test
	void shouldCreateTextPolicyDocument() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocumentEntity next = policyDocumentEntityRepository.findById(saved.getId()).get();

		assertThat(saved.siteId).isEqualTo(next.siteId);
		assertThat(saved.name).isEqualTo(next.name);
		assertThat(saved.workflow).isEqualTo(next.workflow);
		assertThat(saved.revision).isEqualTo(next.revision);
		assertThat(saved.contentType).isEqualTo(next.contentType);
		assertThat(saved.htmlText).isEqualTo(next.htmlText);
		assertThat(saved.file).isEqualTo(next.file);
		assertThat(saved.fileType).isEqualTo(next.fileType);
	}

	@Test
	void shouldCreateFilePolicyDocument() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(1)
			.file(new byte[0])
			.fileType("pdf")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocumentEntity next = policyDocumentEntityRepository.findById(saved.getId()).get();

		assertThat(saved.siteId).isEqualTo(next.siteId);
		assertThat(saved.name).isEqualTo(next.name);
		assertThat(saved.workflow).isEqualTo(next.workflow);
		assertThat(saved.revision).isEqualTo(next.revision);
		assertThat(saved.contentType).isEqualTo(next.contentType);
		assertThat(saved.htmlText).isEqualTo(next.htmlText);
		assertThat(saved.file).isEqualTo(next.file);
		assertThat(saved.fileType).isEqualTo(next.fileType);
	}

	@Test
	void shouldNotCreateWhenContentTypeIs0AndFileIsNotNull() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(1)
			.wysiwygText("sdsadas")
			.file(new byte[0])
			.build();

		assertThrows(Exception.class,() -> policyDocumentEntityRepository.save(policyDocumentEntity));
	}

	@Test
	void shouldNotCreateWhenContentTypeIs1AndTextIsNotNull() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(1)
			.wysiwygText("sdsadas")
			.build();

		assertThrows(Exception.class,() -> policyDocumentEntityRepository.save(policyDocumentEntity));
	}

	@Test
	void shouldUpdate() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		PolicyDocumentEntity policyDocumentEntity1 = PolicyDocumentEntity.builder()
			.id(saved.getId())
			.siteId(siteId)
			.name("name2")
			.workflow(1)
			.revision(1)
			.contentType(1)
			.file(new byte[0])
			.fileType("pdf")
			.build();

		PolicyDocumentEntity saved1 = policyDocumentEntityRepository.save(policyDocumentEntity);
		PolicyDocumentEntity next = policyDocumentEntityRepository.findById(saved.getId()).get();


		assertThat(saved1.siteId).isEqualTo(next.siteId);
		assertThat(saved1.name).isEqualTo(next.name);
		assertThat(saved1.workflow).isEqualTo(next.workflow);
		assertThat(saved1.revision).isEqualTo(next.revision);
		assertThat(saved1.contentType).isEqualTo(next.contentType);
		assertThat(saved1.htmlText).isEqualTo(next.htmlText);
		assertThat(saved1.file).isEqualTo(next.file);
		assertThat(saved1.fileType).isEqualTo(next.fileType);
	}

	@Test
	void shouldDelete() {
		PolicyDocumentEntity policyDocumentEntity = PolicyDocumentEntity.builder()
			.siteId(siteId)
			.name("name")
			.workflow(0)
			.revision(0)
			.contentType(0)
			.wysiwygText("sdsadas")
			.build();

		PolicyDocumentEntity saved = policyDocumentEntityRepository.save(policyDocumentEntity);

		policyDocumentEntityRepository.deleteById(saved.getId());

		assertThat(policyDocumentEntityRepository.findById(saved.getId())).isEmpty();
	}
}