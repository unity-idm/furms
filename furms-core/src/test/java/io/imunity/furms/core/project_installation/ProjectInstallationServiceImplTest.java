/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_installation.ProjectInstallationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.SEND;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

class ProjectInstallationServiceImplTest {
	@Mock
	private ProjectInstallationRepository repository;
	@Mock
	private UsersDAO usersDAO;

	private ProjectInstallationServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectInstallationServiceImpl(repository, usersDAO);
		orderVerifier = inOrder(repository);
	}

	@Test
	void shouldCreateProjectInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectInstallationJob projectInstallationJob = ProjectInstallationJob.builder()
				.correlationId(id)
				.status(SEND)
				.build();

		//when
		service.create("communityId", projectInstallationJob);

		//then
		orderVerifier.verify(repository).create(eq(projectInstallationJob));
	}

	@Test
	void shouldUpdateProjectInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectInstallationJob projectInstallationJob = ProjectInstallationJob.builder()
				.id("id")
				.correlationId(id)
				.status(SEND)
				.build();

		//when
		when(repository.findByCorrelationId(id)).thenReturn(projectInstallationJob);
		service.updateStatus(id, SEND);

		//then
		orderVerifier.verify(repository).update("id", SEND);
	}

	@Test
	void shouldDeleteProjectInstallation() {
		service.delete("id", "id");

		orderVerifier.verify(repository).delete(eq("id"));
	}
}