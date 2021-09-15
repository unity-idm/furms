/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class SiteCreditsCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForSiteCreditsList() {
		final String siteId = "siteId";
		runCLI("site", "credit", "list", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/credits", siteId)));
	}

	@Test
	void shouldCallForSpecificSiteCredit() {
		final String siteId = "siteId";
		final String creditId = "creditId";
		runCLI("site", "credit", "show", siteId, creditId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/credits/{projectId}", siteId, creditId)));
	}
}