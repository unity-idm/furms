/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_installation.Error;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.site.api.status_updater.ProjectInstallationStatusUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class SiteAgentProjectOperationServiceTest extends IntegrationTestBase {

	@Autowired
	private SiteAgentProjectOperationService siteAgentProjectOperationService;
	@Autowired
	private ProjectInstallationStatusUpdater projectInstallationService;

	@Test
	void shouldInstallProject() {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectInstallation projectInstallation = ProjectInstallation.builder()
			.id(new ProjectId(UUID.randomUUID()))
			.siteId(new SiteId(UUID.randomUUID().toString(), "mock"))
			.communityId(new CommunityId(UUID.randomUUID()))
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
			.id(new ProjectId(UUID.randomUUID()))
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

		siteAgentProjectOperationService.removeProject(correlationId, new SiteExternalId("mock"), new ProjectId(UUID.randomUUID()));
	}
}
