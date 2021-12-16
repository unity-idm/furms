/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.resource_types;

import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.domain.resource_types.ResourceType;
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

class ResourceTypeServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ResourceTypeService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ResourceTypeService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInResourceTypeService() {
		forMethods(
				() -> service.findById(resourceType, site),
				() -> service.findAll(site))
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
		.andForMethods(
				() -> service.findAll())
				.accessFor(
						fenixAdmin())
				.deniedFor(
						basicUser(),
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
				() -> service.create(ResourceType.builder().siteId(site).build()),
				() -> service.update(ResourceType.builder().siteId(site).build()),
				() -> service.delete(resourceType, site))
				.accessFor(
						fenixAdmin(),
						siteAdmin(site))
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
		.verifySecurityRulesAndInterfaceCoverage(ResourceTypeService.class, server);
	}
}
