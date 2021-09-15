/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;

class SiteProtocolMessageCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForSiteProtocolMessageList() {
		final String siteId = "siteId";
		runCLI("site", "protocolMsg", "list", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/protocolMessages", siteId)));
	}

	@Test
	void shouldCallForDeleteProtocolMessages() {
		final String siteId = "siteId";
		final String messageId = "messageId";
		runCLI("site", "protocolMsg", "delete", siteId, messageId);

		server.verify(deleteRequestedFor(restPath("/sites/{siteId}/protocolMessages/{messageId}", siteId, messageId)));
	}

	@Test
	void shouldCallForRetryProtocolMessage() {
		final String siteId = "siteId";
		final String messageId = "messageId";
		runCLI("site", "protocolMsg", "retry", siteId, messageId);

		server.verify(postRequestedFor(restPath("/sites/{siteId}/protocolMessages/{messageId}", siteId, messageId)));
	}
}