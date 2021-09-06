/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.community;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class CommunityCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForCommunitiesList() {
		runCLI("community", "list");

		server.verify(getRequestedFor(restPath("/communities")));
	}

	@Test
	void shouldCallForSpecificCommunity() {
		final String communityId = "communityId";
		runCLI("community", "show", "communityId");

		server.verify(getRequestedFor(restPath("/communities/{communityId}", communityId)));
	}

}