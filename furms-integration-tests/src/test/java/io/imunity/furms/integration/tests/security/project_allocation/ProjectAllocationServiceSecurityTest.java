/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.project_allocation;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProjectAllocationServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ProjectAllocationService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWith_PROJECT_READ_canFindByProjectIdAndId() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findByProjectIdAndId(project, projectAllocation));
	}

	@Test
	void userWith_PROJECT_READ_canFindByIdValidatingProjectsWithRelatedObjects() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findByIdValidatingProjectsWithRelatedObjects(projectAllocation, project));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindByIdWithRelatedObjects() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findByIdWithRelatedObjects(community, projectAllocation));
	}

	@Test
	void userWith_COMMUNITY_READ_canGetOccupiedNames() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.getOccupiedNames(community, projectAllocation));
	}

	@Test
	void userWith_COMMUNITY_READ_canGetAvailableAmount() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.getAvailableAmount(community, communityAllocation));
	}

	@Test
	void userWith_PROJECT_READ_canFindAllWithRelatedObjects() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findAllWithRelatedObjects(community, project));
	}

	@Test
	void userWith_PROJECT_LIMITED_READ_canFindAllUninstallations() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_READ(() -> service.findAllUninstallations(project));
	}

	@Test
	void userWith_PROJECT_LIMITED_READ_canFindAllChunks() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_READ(() -> service.findAllChunks(project));
	}

	@Test
	void userWith_SITE_READ_canFindAllChunksBySiteId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllChunksBySiteId(site));
	}

	@Test
	void userWith_SITE_READ_canFindAllChunksBySiteIdAndProjectId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllChunksBySiteIdAndProjectId(site, project));
	}

	@Test
	void userWith_PROJECT_LIMITED_READ_canFindAllInstallations() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_READ(() -> service.findAllInstallations(project));
	}

	@Test
	void userWith_PROJECT_LIMITED_READ_canFindAllWithRelatedObjects() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_READ(() -> service.findAllWithRelatedObjects(project));
	}

	@Test
	void userWith_SITE_READ_canFindAllWithRelatedObjectsBySiteIdAndProjectId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllWithRelatedObjectsBySiteIdAndProjectId(site, project));
	}

	@Test
	void userWith_PROJECT_READ_canFindAll() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findAll(community, project));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canCreate() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.create(community, ProjectAllocation.builder().build()));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.update(community, ProjectAllocation.builder().build()));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canDelete() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.delete(community, projectAllocation));
	}

}