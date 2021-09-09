/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class SiteResourceTypeCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForSiteResourceTypeList() {
		final String siteId = "siteId";
		runCLI("site", "resourceType", "list", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/resourceTypes", siteId)));
	}

	@Test
	void shouldCallForAckSiteResourceType() {
		final String siteId = "siteId";
		final String resourceTypeId = "resourceTypeId";
		runCLI("site", "resourceType", "show", siteId, resourceTypeId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/resourceTypes/{resourceTypeId}", siteId, resourceTypeId)));
	}
}