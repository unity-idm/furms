/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class SiteAllocationsCommandTest  extends CLICommandsTest {

	@Test
	void shouldCallForSiteAllocationsList() {
		final String siteId = "siteId";
		runCLI("site", "allocations", "list", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/siteAllocations", siteId)));
	}

	@Test
	void shouldCallForSpecificSiteAllocationsForProject() {
		final String siteId = "siteId";
		final String projectId = "projectId";
		runCLI("site", "allocations", "show", siteId, projectId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/siteAllocations/{projectId}", siteId, projectId)));
	}
}