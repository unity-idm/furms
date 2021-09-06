/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class SitePolicyCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForSitePolicyList() {
		final String siteId = "siteId";
		runCLI("site", "policy", "list", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/policies", siteId)));
	}

	@Test
	void shouldCallForAckSitePolicy() {
		final String siteId = "siteId";
		final String policyId = "policyId";
		runCLI("site", "policy", "show", siteId, policyId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/policies/{policyId}", siteId, policyId)));
	}
}