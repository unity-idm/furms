/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectInstallationsServiceImplTest {
	@Mock
	private ProjectOperationRepository projectOperationRepository;
	@InjectMocks
	private ProjectInstallationsServiceImpl service;

	@Test
	void findAllByCommunityId() {
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		service.findAllByCommunityId(communityId);
		verify(projectOperationRepository).findAllByCommunityId(communityId);
	}

	@Test
	void findAllUpdatesByCommunityId() {
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		service.findAllUpdatesByCommunityId(communityId);
		verify(projectOperationRepository).findAllUpdatesByCommunityId(communityId);
	}

	@Test
	void findAllByProjectId() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		service.findAllByProjectId(projectId);
		verify(projectOperationRepository).findAllByProjectId(projectId);
	}

	@Test
	void findAllUpdatesByProjectId() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		service.findAllUpdatesByProjectId(projectId);
		verify(projectOperationRepository).findAllUpdatesByProjectId(projectId);
	}
}