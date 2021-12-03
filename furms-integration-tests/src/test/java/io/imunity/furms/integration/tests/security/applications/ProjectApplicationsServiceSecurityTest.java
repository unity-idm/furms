/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.applications;

import io.imunity.furms.api.applications.ProjectApplicationsService;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;

class ProjectApplicationsServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ProjectApplicationsService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWith_PROJECT_LIMITED_READ_forProjectCanFindAllApplyingUsers() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_READ(() -> service.findAllApplyingUsers(project));
	}

	@Test
	void userWith_AUTHENTICATED_canFindAllApplicationsUsersForCurrentProjectAdmins() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findAllApplicationsUsersForCurrentProjectAdmins());
	}

	@Test
	void userWith_AUTHENTICATED_canFindAllAppliedProjectsIdsForCurrentUser() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findAllAppliedProjectsIdsForCurrentUser());
	}

	@Test
	void userWith_AUTHENTICATED_canCreateForCurrentUser() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.createForCurrentUser(project));
	}

	@Test
	void userWith_AUTHENTICATED_canRemoveForCurrentUser() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.removeForCurrentUser(project));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canAccept() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.accept(project, new FenixUserId(basicUser().getFenixId())));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canRemove() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.remove(project, new FenixUserId(basicUser().getFenixId())));
	}

}
