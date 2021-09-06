/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class SiteFurmsAllocationsCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForSiteCreditsList() {
		final String siteId = "siteId";
		runCLI("site", "furmsAllocations", "list", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/furmsAllocations", siteId)));
	}

	@Test
	void shouldCallForSpecificSiteCredit() {
		final String siteId = "siteId";
		final String projectId = "projectId";
		runCLI("site", "furmsAllocations", "show", siteId, projectId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/furmsAllocations/{projectId}", siteId, projectId)));
	}
}