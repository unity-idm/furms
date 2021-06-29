/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectInstallationStatusServiceImplTest {
	@Mock
	private ProjectOperationRepository projectOperationRepository;
	@InjectMocks
	private ProjectInstallationStatusServiceImpl service;

	@Test
	void findAllByCommunityId() {
		service.findAllByCommunityId("id");
		verify(projectOperationRepository).findAllByCommunityId("id");
	}

	@Test
	void findAllUpdatesByCommunityId() {
		service.findAllUpdatesByCommunityId("id");
		verify(projectOperationRepository).findAllUpdatesByCommunityId("id");
	}

	@Test
	void findAllByProjectId() {
		service.findAllByProjectId("id");
		verify(projectOperationRepository).findAllByProjectId("id");
	}

	@Test
	void findAllUpdatesByProjectId() {
		service.findAllUpdatesByProjectId("id");
		verify(projectOperationRepository).findAllUpdatesByProjectId("id");
	}
}