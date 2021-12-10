/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.generic_group;

import io.imunity.furms.api.generic_groups.GenericGroupService;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;

class GenericGroupServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private GenericGroupService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(GenericGroupService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInGenericGroupService() {
		final GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID().toString());

		forMethods(
				() -> service.findAll(community, genericGroupId),
				() -> service.createMembership(community, genericGroupId, fenixId),
				() -> service.deleteMembership(community, genericGroupId, fenixId))
				.accessFor(
						communityAdmin(community))
				.deniedFor(
						basicUser(),
						fenixAdmin(),
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
				() -> service.findAll(community),
				() -> service.findGroupWithAssignments(community, genericGroupId),
				() -> service.findBy(community, genericGroupId),
				() -> service.findAllGroupWithAssignmentsAmount(community),
				() -> service.create(GenericGroup.builder().communityId(community).build()),
				() -> service.update(GenericGroup.builder().communityId(community).build()),
				() -> service.delete(community, genericGroupId))
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
	}

}