/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.project;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;

class ProjectAllocationCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForProjectAllocationsList() {
		final String projectId = "projectId";
		runCLI("project", "allocation", "list", projectId);

		server.verify(getRequestedFor(restPath("/projects/{projectId}/allocations", projectId)));
	}

	@Test
	void shouldCallForSpecificProjectAllocation() {
		final String projectId = "projectId";
		final String allocationId = "allocationId";
		runCLI("project", "allocation", "show", projectId, allocationId);

		server.verify(getRequestedFor(restPath("/projects/{projectId}/allocations/{projectAllocationId}",
				projectId, allocationId)));
	}

	@Test
	void shouldCallForCreateProjectAllocation() {
		final String projectId = "projectId";
		final File file = createDefaultDefinitionFile();
		runCLI("project", "allocation", "create", projectId, "--file", file.getPath());

		try {
			server.verify(postRequestedFor(restPath("/projects/{projectId}/allocations", projectId))
					.withRequestBody(equalToJson(DEFAULT_DEFINITION_FILE_BODY)));
		} finally {
			file.delete();
		}
	}
}