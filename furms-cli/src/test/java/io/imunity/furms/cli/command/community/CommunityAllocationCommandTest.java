/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.community;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;

class CommunityAllocationCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForCommunityAllocationsList() {
		final String communityId = "communityId";
		runCLI("community", "allocation", "list", communityId);

		server.verify(getRequestedFor(restPath("/communities/{communityId}/allocations", communityId)));
	}

	@Test
	void shouldCallForSpecificCommunityAllocation() {
		final String communityId = "communityId";
		final String allocationId = "allocationId";
		runCLI("community", "allocation", "show", communityId, allocationId);

		server.verify(getRequestedFor(restPath("/communities/{communityId}/allocations/{communityAllocationId}",
				communityId, allocationId)));
	}

	@Test
	void shouldCallForCreateCommunityAllocation() {
		final String communityId = "communityId";
		final File file = createDefaultDefinitionFile();
		runCLI("community", "allocation", "create", communityId, "--file", file.getPath());

		try {
			server.verify(postRequestedFor(restPath("/communities/{communityId}/allocations", communityId))
					.withRequestBody(equalToJson(DEFAULT_DEFINITION_FILE_BODY)));
		} finally {
			file.delete();
		}
	}

}