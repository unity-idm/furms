/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.community;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class CommunityProjectCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForCommunityProjectsList() {
		final String communityId = "communityId";
		runCLI("community", "project", "list", communityId);

		server.verify(getRequestedFor(restPath("/communities/{communityId}/projects", communityId)));
	}

}