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

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;

class ProjectAllocationServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ProjectAllocationService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ProjectAllocationService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInProjectAllocationService() {
		forMethods(
				() -> service.findAllUninstallations(project),
				() -> service.findAllChunks(project),
				() -> service.findAllChunks(project, projectAllocation),
				() -> service.findAllInstallations(project),
				() -> service.findByIdValidatingProjectsWithRelatedObjects(projectAllocation, project),
				() -> service.findAllWithRelatedObjects(project))
				.accessFor(
						basicUser(),
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(community),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.andForMethods(
				() -> service.findByProjectIdAndId(project, projectAllocation),
				() -> service.findAllWithRelatedObjects(community, project),
				() -> service.findAll(community, project))
				.accessFor(
						communityAdmin(community),
						projectAdmin(community, project),
						projectUser(community, project))
				.deniedFor(
						basicUser(),
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(otherCommunity, otherProject),
						projectUser(otherCommunity, otherProject))
		.andForMethods(
				() -> service.findByIdWithRelatedObjects(community, projectAllocation),
				() -> service.getOccupiedNames(community, project),
				() -> service.getAvailableAmount(community, communityAllocation),
				() -> service.create(community, ProjectAllocation.builder().build()),
				() -> service.update(community, ProjectAllocation.builder().build()),
				() -> service.delete(community, projectAllocation))
				.accessFor(
						fenixAdmin(),
						communityAdmin(community))
				.deniedFor(
						basicUser(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.andForMethods(
				() -> service.findAllChunksBySiteId(site),
				() -> service.findAllChunksBySiteIdAndProjectId(site, project),
				() -> service.findAllWithRelatedObjectsBySiteId(site),
				() -> service.findAllWithRelatedObjectsBySiteIdAndProjectId(site, project))
				.accessFor(
						fenixAdmin(),
						siteAdmin(site),
						siteSupport(site))
				.deniedFor(
						basicUser(),
						siteAdmin(otherSite),
						siteSupport(otherSite),
						communityAdmin(community),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.verifySecurityRulesAndInterfaceCoverage(ProjectAllocationService.class, server);
	}

}