/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;

class SitePolicyAcceptanceCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForSitePolicyAcceptancesList() {
		final String siteId = "siteId";
		runCLI("site", "policy", "acceptance", "list", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/policyAcceptances", siteId)));
	}

	@Test
	void shouldCallForAckSitePolicyAcceptance() {
		final String siteId = "siteId";
		final String policyId = "policyId";
		final String fenixUserId = "fenixUserId";
		runCLI("site", "policy", "acceptance", "ack", siteId, policyId, fenixUserId);

		server.verify(postRequestedFor(restPath("/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}",
				siteId, policyId, fenixUserId)));
	}
}