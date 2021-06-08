/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.Error;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationStatusUpdater;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SiteAgentProjectOperationServiceTest {

	@Autowired
	private SiteAgentProjectOperationService siteAgentProjectOperationService;
	@Autowired
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@Autowired
	private ProjectInstallationStatusUpdater projectInstallationService;

	@BeforeEach
	void init(){
		siteAgentListenerConnector.connectListenerToQueue( "mock-site-pub");
	}

	@Test
	void shouldInstallProject() {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectInstallation projectInstallation = ProjectInstallation.builder()
			.id("id")
			.siteExternalId("mock")
			.validityStart(LocalDateTime.now())
			.validityEnd(LocalDateTime.now())
			.leader(FURMSUser.builder()
				.id(new PersistentId("id"))
				.email("email")
				.build())
			.build();
		siteAgentProjectOperationService.installProject(correlationId, projectInstallation);
		verify(projectInstallationService, timeout(10000)).update(
			correlationId,
			new ProjectInstallationResult(Map.of(), ProjectInstallationStatus.ACKNOWLEDGED, new Error(null, null))
		);
		verify(projectInstallationService, timeout(10000)).update(
			correlationId,
			new ProjectInstallationResult(Map.of("gid", "1"), ProjectInstallationStatus.INSTALLED, new Error(null, null))
		);
	}

	@Test
	void shouldUpdateProject() {
		CorrelationId correlationId = CorrelationId.randomID();
		PersistentId userId = new PersistentId("id");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.email("email")
			.build();
		Project project = Project.builder()
			.id("id")
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.leaderId(userId)
			.build();
		siteAgentProjectOperationService.updateProject(correlationId, new SiteExternalId("mock"), project, user);

		verify(projectInstallationService, timeout(10000)).update(
			correlationId,
			new ProjectUpdateResult(ProjectUpdateStatus.ACKNOWLEDGED, new Error(null, null))
		);
		verify(projectInstallationService, timeout(10000)).update(
			correlationId,
			new ProjectUpdateResult(ProjectUpdateStatus.UPDATED, new Error(null, null))
		);
	}

	@Test
	void shouldRemoveProject() {
		CorrelationId correlationId = CorrelationId.randomID();

		siteAgentProjectOperationService.removeProject(correlationId, new SiteExternalId("mock"), "projectId");
	}
}
