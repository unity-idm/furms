/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SiteEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteEntityRepository siteEntityRepository;
	@Autowired
	private PolicyDocumentRepository policyDocumentRepository;

	@BeforeEach
	void setUp() {
		siteEntityRepository.deleteAll();
	}

	@Test
	void shouldCreateSiteEntity() {
		//given
		SiteEntity entityToSave = SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build();

		//when
		SiteEntity saved = siteEntityRepository.save(entityToSave);

		//then
		assertThat(siteEntityRepository.findAll()).hasSize(1);
		assertThat(siteEntityRepository.findById(saved.getId())).isPresent();
	}

	@Test
	void shouldUpdateSiteEntity() {
		//given
		SiteEntity old = siteEntityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());

		PolicyId policyId = policyDocumentRepository.create(PolicyDocument.builder()
			.siteId(old.getId().toString())
			.name("policyName")
			.wysiwygText("wysiwygText")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.EMBEDDED)
			.build());

		SiteEntity toUpdate = SiteEntity.builder()
				.id(old.getId())
				.name("new_name")
				.externalId("id2")
				.policyId(policyId.id)
				.build();

		//when
		siteEntityRepository.save(toUpdate);

		//then
		Optional<SiteEntity> byId = siteEntityRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
		assertThat(byId.get().getPolicyId()).isEqualTo(policyId.id);
	}

	@Test
	void shouldFindSiteById() {
		//given
		SiteEntity toFind = siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.externalId("id")
				.build());

		//when
		Optional<SiteEntity> byId = siteEntityRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindSiteBySiteId() {
		//given
		SiteEntity toFind = siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.externalId("id")
				.build());

		//when
		Optional<SiteEntity> bySiteId = siteEntityRepository.findById(toFind.getId());

		//then
		assertThat(bySiteId).isPresent();
	}

	@Test
	void shouldFindAllAvailableSites() {
		//given
		siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.externalId("id")
				.build());
		siteEntityRepository.save(SiteEntity.builder()
				.name("name2")
				.externalId("id2")
				.build());

		//when
		Iterable<SiteEntity> all = siteEntityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCheckIfExistsBySiteId() {
		//given
		SiteEntity site = siteEntityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());

		//when + then
		assertThat(siteEntityRepository.existsById(site.getId())).isTrue();
		assertThat(siteEntityRepository.existsById(generateId())).isFalse();
	}

	@Test
	void shouldCheckIfExistsByName() {
		//given
		SiteEntity site = siteEntityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());

		//when
		boolean exists = siteEntityRepository.existsByName(site.getName());
		boolean nonExists = siteEntityRepository.existsByName("wrong_name");

		//then
		assertThat(exists).isTrue();
		assertThat(nonExists).isFalse();
	}

	@Test
	void shouldDeleteEntity() {
		//given
		SiteEntity entityToRemove = siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.externalId("id")
				.build());

		//when
		siteEntityRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(siteEntityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteEntityBySiteId() {
		//given
		SiteEntity entityToRemove = siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.externalId("id")
				.build());

		//when
		siteEntityRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(siteEntityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteAllEntities() {
		//given
		siteEntityRepository.save(SiteEntity.builder()
				.name("name1")
				.externalId("id")
				.build());
		siteEntityRepository.save(SiteEntity.builder()
				.name("name2")
				.externalId("id2")
				.build());

		//when
		siteEntityRepository.deleteAll();

		//then
		assertThat(siteEntityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldReturnTrueIfNameIsPresentOutOfSpecificRecord() {
		//given
		SiteEntity entity = siteEntityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());
		SiteEntity entity2 = siteEntityRepository.save(SiteEntity.builder()
				.name("name2")
				.externalId("id2")
				.build());

		//when + then
		assertThat(siteEntityRepository.existsByNameAndIdIsNot(entity.getName(), entity2.getId())).isTrue();
	}

	@Test
	void shouldReturnFalseIfNameIsPresentOnlyInSpecificRecord() {
		//given
		SiteEntity entity = siteEntityRepository.save(SiteEntity.builder()
				.name("name")
				.externalId("id")
				.build());

		//when + then
		assertThat(siteEntityRepository.existsByNameAndIdIsNot("otherName", entity.getId())).isFalse();
	}

}