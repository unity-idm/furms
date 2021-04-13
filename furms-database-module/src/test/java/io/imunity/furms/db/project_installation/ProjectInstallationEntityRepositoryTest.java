/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;


import io.imunity.furms.db.DBIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACK;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.SEND;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectInstallationEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private ProjectInstallationJobEntityRepository entityRepository;

	@AfterEach
	void clean(){
		entityRepository.deleteAll();
	}

	@Test
	void shouldCreateProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity entityToSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(UUID.randomUUID())
				.projectId(UUID.randomUUID())
				.status(SEND)
				.build();

		//when
		ProjectInstallationJobEntity saved = entityRepository.save(entityToSave);

		//then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());
		assertThat(byId.get().status).isEqualTo(SEND);
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldUpdateProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity entityToSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(UUID.randomUUID())
				.projectId(UUID.randomUUID())
				.status(SEND)
				.build();


		//when
		ProjectInstallationJobEntity save = entityRepository.save(entityToSave);
		ProjectInstallationJobEntity entityToUpdate = ProjectInstallationJobEntity.builder()
			.correlationId(save.correlationId)
			.siteId(save.siteId)
			.projectId(save.projectId)
			.status(ACK)
			.build();

		entityRepository.save(entityToUpdate);

		//then
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(entityToSave.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(save.getId());
		assertThat(byId.get().status).isEqualTo(ACK);
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldFindCreatedProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toFind = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(UUID.randomUUID())
				.projectId(UUID.randomUUID())
				.status(SEND)
				.build();

		entityRepository.save(toFind);

		//when
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(toFind.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindCreatedProjectInstallationJobByCorrelationId() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toFind = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(UUID.randomUUID())
				.projectId(UUID.randomUUID())
				.status(SEND)
				.build();

		entityRepository.save(toFind);
		ProjectInstallationJobEntity findById = entityRepository.findByCorrelationId(correlationId);

		//when
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(findById.getId());

		//then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(UUID.randomUUID())
				.projectId(UUID.randomUUID())
				.status(SEND)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectInstallationJobEntity toSave1 = ProjectInstallationJobEntity.builder()
			.correlationId(correlationId1)
			.siteId(UUID.randomUUID())
			.projectId(UUID.randomUUID())
			.status(ACK)
			.build();

		entityRepository.save(toSave);
		entityRepository.save(toSave1);

		//when
		Iterable<ProjectInstallationJobEntity> all = entityRepository.findAll();

		//then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldDeleteProjectInstallationJob() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(UUID.randomUUID())
				.projectId(UUID.randomUUID())
				.status(SEND)
				.build();

		//when
		entityRepository.save(toSave);
		entityRepository.deleteById(toSave.getId());

		//then
		assertThat(entityRepository.findById(toSave.getId())).isEmpty();
	}

	@Test
	void shouldDeleteAllProjectInstallationJobs() {
		//given
		UUID correlationId = UUID.randomUUID();
		ProjectInstallationJobEntity toSave = ProjectInstallationJobEntity.builder()
				.correlationId(correlationId)
				.siteId(UUID.randomUUID())
				.projectId(UUID.randomUUID())
				.status(SEND)
				.build();
		UUID correlationId1 = UUID.randomUUID();
		ProjectInstallationJobEntity toSave1 = ProjectInstallationJobEntity.builder()
			.correlationId(correlationId1)
			.siteId(UUID.randomUUID())
			.projectId(UUID.randomUUID())
			.status(ACK)
			.build();

		//when
		entityRepository.save(toSave);
		entityRepository.save(toSave1);
		entityRepository.deleteAll();

		//then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}