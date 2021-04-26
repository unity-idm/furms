/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_installation.ProjectInstallationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.SENT;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

class ProjectInstallationMessageResolverTest {
	@Mock
	private ProjectInstallationRepository repository;

	private ProjectInstallationMessageResolverImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectInstallationMessageResolverImpl(repository);
		orderVerifier = inOrder(repository);
	}


	@Test
	void shouldUpdateProjectInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectInstallationJob projectInstallationJob = ProjectInstallationJob.builder()
				.id("id")
				.correlationId(id)
				.status(SENT)
				.build();

		//when
		when(repository.findByCorrelationId(id)).thenReturn(projectInstallationJob);
		service.updateStatus(id, SENT);

		//then
		orderVerifier.verify(repository).update("id", SENT);
	}
}