/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class SiteServiceCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForSiteServicesList() {
		final String siteId = "siteId";
		runCLI("site", "service", "list", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/services", siteId)));
	}

	@Test
	void shouldCallForAckSiteService() {
		final String siteId = "siteId";
		final String policyId = "policyId";
		runCLI("site", "service", "show", siteId, policyId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/services/{policyId}", siteId, policyId)));
	}
}