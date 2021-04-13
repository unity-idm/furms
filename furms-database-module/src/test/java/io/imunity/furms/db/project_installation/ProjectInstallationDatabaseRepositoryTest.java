/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.site_agent.CorrelationId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.DONE;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.SEND;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectInstallationDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private ProjectInstallationJobEntityRepository entityRepository;

	@Autowired
	private ProjectInstallationJobDatabaseRepository entityDatabaseRepository;

	@Test
	void shouldCreateProjectAllocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.correlationId(correlationId)
				.siteId(UUID.randomUUID().toString())
				.projectId(UUID.randomUUID().toString())
				.status(SEND)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);

		//then
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(SEND);
	}

	@Test
	void shouldUpdateProjectAllocation() {
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.id("id")
				.correlationId(correlationId)
				.siteId(UUID.randomUUID().toString())
				.projectId(UUID.randomUUID().toString())
				.status(SEND)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update("id", DONE);

		//then
		Optional<ProjectInstallationJobEntity> byId = entityRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(DONE);
	}

	@Test
	void shouldRemoveProjectInstallationJob(){
		//given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		ProjectInstallationJob request = ProjectInstallationJob.builder()
				.correlationId(correlationId)
				.siteId(UUID.randomUUID().toString())
				.projectId(UUID.randomUUID().toString())
				.status(SEND)
				.build();

		//when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.delete(id);

		//then
		assertThat(entityRepository.findById(UUID.fromString(id))).isEmpty();
	}

}