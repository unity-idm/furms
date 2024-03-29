/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.resource_usage;

import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;
import static java.time.LocalDateTime.now;

class ResourceUsageServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ResourceUsageService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ResourceUsageService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInResourceUsageService() {
		forMethods(
				() -> service.findAllUserUsages(site, Set.of(), now(), now()))
				.accessFor(
						fenixAdmin(),
						siteAdmin(site)
				)
				.deniedFor(
						basicUser(),
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
				() -> service.findAllUserUsagesHistory(project, projectAllocation),
				() -> service.findAllResourceUsageHistory(project, projectAllocation))
				.accessFor(
					fenixAdmin(),
					communityAdmin(community),
					projectAdmin(community, project),
					projectUser(community, project)
				)
				.deniedFor(
					siteAdmin(site),
					siteAdmin(otherSite),
					siteSupport(site),
					siteSupport(otherSite),
					basicUser(),
					communityAdmin(otherCommunity),
					projectAdmin(otherCommunity, otherProject),
					projectUser(otherCommunity, otherProject)
					)
			.andForMethods(
					() -> service.findAllResourceUsageHistoryByCommunity(community, communityAllocation))
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
		.verifySecurityRulesAndInterfaceCoverage(ResourceUsageService.class, server);
	}
}
