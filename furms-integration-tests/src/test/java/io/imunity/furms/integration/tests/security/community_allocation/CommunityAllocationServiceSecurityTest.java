/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.community_allocation;

import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
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

class CommunityAllocationServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private CommunityAllocationService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(CommunityAllocationService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInCommunityAllocationService() {
		forMethods(
				() -> service.findByCommunityIdAndIdWithRelatedObjects(community, communityAllocation),
				() -> service.findAllByCommunityId(community),
				() -> service.getOccupiedNames(community),
				() -> service.findAllWithRelatedObjects(community),
				() -> service.findAllWithRelatedObjects(community, "name", false, false),
				() -> service.findAllNotExpiredByCommunityIdWithRelatedObjects(community))
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
				.validate(server);
		forMethods(
				() -> service.findById(communityAllocation),
				() -> service.findByIdWithRelatedObjects(communityAllocation),
				() -> service.findAll(),
				() -> service.getAvailableAmountForNew(resourceCredit),
				() -> service.getAvailableAmountForUpdate(resourceCredit, communityAllocation),
				() -> service.create(CommunityAllocation.builder().build()),
				() -> service.update(CommunityAllocation.builder().build()),
				() -> service.delete(communityAllocation))
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
	}
}