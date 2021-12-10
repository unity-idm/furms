/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.resource_credits;

import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
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

class ResourceCreditServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ResourceCreditService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ResourceCreditService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInResourceCreditService() {
		forMethods(
				() -> service.findWithAllocationsByIdAndSiteId(resourceCredit, site),
				() -> service.findAllWithAllocations(site))
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
				.validate(server);
		forMethods(
				() -> service.findAll(),
				() -> service.findAllNotExpiredByResourceTypeId(resourceType),
				() -> service.findAllWithAllocations("name", false, false))
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
				.validate(server);
		forMethods(
				() -> service.create(ResourceCredit.builder().siteId(site).build()),
				() -> service.update(ResourceCredit.builder().siteId(site).build()),
				() -> service.delete(resourceCredit, site),
				() -> service.hasCommunityAllocations(resourceCredit, site),
				() -> service.getOccupiedNames(site))
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
				.validate(server);
	}
}
