/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InfraServiceEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private InfraServiceEntityRepository serviceRepository;
	@Autowired
	private PolicyDocumentRepository policyDocumentRepository;

	private SiteId siteId;
	private SiteId siteId2;


	@BeforeEach
	void init() {
		Site community = Site.builder()
			.name("name")
			.build();
		Site community2 = Site.builder()
			.name("name2")
			.build();
		siteId = siteRepository.create(community, new SiteExternalId("id"));
		siteId2 = siteRepository.create(community2, new SiteExternalId("id2"));
	}

	@Test
	void shouldCreateInfraService() {
		//given
		PolicyId policyId = policyDocumentRepository.create(PolicyDocument.builder()
			.siteId(siteId)
			.name("policyName")
			.wysiwygText("wysiwygText")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.EMBEDDED)
			.build());

		InfraServiceEntity entityToSave = InfraServiceEntity.builder()
			.name("name")
			.siteId(siteId.id)
			.description("description")
			.policyId(policyId.id)
			.build();

		//when
		InfraServiceEntity saved = serviceRepository.save(entityToSave);

		//then
		assertThat(serviceRepository.findAll()).hasSize(1);
		Optional<InfraServiceEntity> byId = serviceRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId.id);
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().description).isEqualTo("description");
	}

	@Test
	void shouldUpdateInfraService() {
		//given
		InfraServiceEntity old = InfraServiceEntity.builder()
			.name("name")
			.siteId(siteId.id)
			.description("description")
			.build();
		serviceRepository.save(old);
		PolicyId policyId = policyDocumentRepository.create(PolicyDocument.builder()
			.siteId(siteId)
			.name("policyName")
			.wysiwygText("wysiwygText")
			.workflow(PolicyWorkflow.WEB_BASED)
			.contentType(PolicyContentType.EMBEDDED)
			.build());

		InfraServiceEntity toUpdate = InfraServiceEntity.builder()
			.name("new_name")
			.siteId(siteId2.id)
			.description("new_description")
			.policyId(policyId.id)
			.build();

		//when
		serviceRepository.save(toUpdate);

		//then
		Optional<InfraServiceEntity> byId = serviceRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().siteId).isEqualTo(siteId2.id);
		assertThat(byId.get().name).isEqualTo("new_name");
		assertThat(byId.get().description).isEqualTo("new_description");
		assertThat(byId.get().policyId).isEqualTo(policyId.id);
	}

	@Test
	void shouldFindCreatedInfraServices() {
		//given
		InfraServiceEntity toFind = InfraServiceEntity.builder()
			.name("name")
			.siteId(siteId.id)
			.description("description")
			.build();
		serviceRepository.save(toFind);

		//when
		Optional<InfraServiceEntity> byId = serviceRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableInfraServices() {
		//given
		serviceRepository.save(InfraServiceEntity.builder()
			.name("name")
			.siteId(siteId.id)
			.description("description")
			.build()
		);
		serviceRepository.save(InfraServiceEntity.builder()
			.name("new_name")
			.siteId(siteId2.id)
			.description("new_description")
			.build()
		);

		//when
		Iterable<InfraServiceEntity> all = serviceRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void savedServiceExistsByInfraServiceId() {
		//given
		InfraServiceEntity service = serviceRepository.save(InfraServiceEntity.builder()
			.siteId(siteId.id)
			.name("name")
			.description("description")
			.build());

		//when + then
		assertThat(serviceRepository.existsById(service.getId())).isTrue();
		assertThat(serviceRepository.existsById(generateId())).isFalse();
	}

	@Test
	void savedInfraServiceExistsByName() {
		//given
		InfraServiceEntity service = serviceRepository.save(InfraServiceEntity.builder()
			.name("name")
			.siteId(siteId.id)
			.description("description")
			.build());

		//when
		boolean exists = serviceRepository.existsByNameAndSiteId(service.name, siteId.id);

		//then
		assertThat(exists).isTrue();
	}

	@Test
	void savedInfraServiceDoesNotExistByName() {
		//given
		serviceRepository.save(InfraServiceEntity.builder()
			.name("name")
			.siteId(siteId.id)
			.description("description")
			.build());

		//when
		boolean exists = serviceRepository.existsByNameAndSiteId("wrong_name", siteId.id);

		//then
		assertThat(exists).isFalse();
	}

	@Test
	void shouldDeleteInfraService() {
		//given
		InfraServiceEntity entityToRemove = serviceRepository.save(InfraServiceEntity.builder()
			.name("name")
			.siteId(siteId.id)
			.description("description")
			.build());

		//when
		serviceRepository.deleteById(entityToRemove.getId());

		//then
		assertThat(serviceRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldInfraDeleteAllServices() {
		//given
		serviceRepository.save(InfraServiceEntity.builder()
			.name("name")
			.siteId(siteId.id)
			.description("description")
			.build());
		serviceRepository.save(InfraServiceEntity.builder()
			.name("new_name")
			.siteId(siteId2.id)
			.description("new_description")
			.build());

		//when
		serviceRepository.deleteAll();

		//then
		assertThat(serviceRepository.findAll()).hasSize(0);
	}

}