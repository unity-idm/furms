/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.alarms;

import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
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

class AlarmServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private AlarmService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(AlarmService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInProjectApplicationsService() {
		final AlarmId alarmId = new AlarmId(UUID.randomUUID());
		final AlarmWithUserEmails alarm = AlarmWithUserEmails.builder().projectId(project).build();
		forMethods(
				() -> service.findAll(project),
				() -> service.find(project, alarmId))
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
				() -> service.create(alarm),
				() -> service.update(alarm),
				() -> service.remove(project, alarmId))
				.accessFor(
						communityAdmin(community),
						projectAdmin(community, project))
				.deniedFor(
						basicUser(),
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.verifySecurityRulesAndInterfaceCoverage(AlarmService.class, server);
	}

}
