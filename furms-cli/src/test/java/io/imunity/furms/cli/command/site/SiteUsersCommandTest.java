/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class SiteUsersCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForSiteUsersList() {
		final String siteId = "siteId";
		runCLI("site", "users", "list", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/users", siteId)));
	}

}