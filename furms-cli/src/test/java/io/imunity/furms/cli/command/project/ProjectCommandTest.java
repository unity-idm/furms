/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.project;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;

class ProjectCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForProjectsList() {
		runCLI("project", "list");

		server.verify(getRequestedFor(restPath("/projects")));
	}

	@Test
	void shouldCallForSpecificProject() {
		final String projectId = "projectId";
		runCLI("project", "show", projectId);

		server.verify(getRequestedFor(restPath("/projects/{projectId}", projectId)));
	}

	@Test
	void shouldCallForDeleteSpecificProject() {
		final String projectId = "projectId";
		runCLI("project", "delete", projectId);

		server.verify(deleteRequestedFor(restPath("/projects/{projectId}", projectId)));
	}

	@Test
	void shouldCallForCreateProject() {
		final File file = createDefaultDefinitionFile();
		runCLI("project", "create", "--file", file.getPath());

		try {
			server.verify(postRequestedFor(restPath("/projects"))
					.withRequestBody(equalToJson(DEFAULT_DEFINITION_FILE_BODY)));
		} finally {
			file.delete();
		}
	}

	@Test
	void shouldCallForUpdateProject() {
		final String projectId = "projectId";
		final File file = createDefaultDefinitionFile();
		runCLI("project", "update", projectId, "--file", file.getPath());

		try {
			server.verify(putRequestedFor(restPath("/projects/{projectId}", projectId))
					.withRequestBody(equalToJson(DEFAULT_DEFINITION_FILE_BODY)));
		} finally {
			file.delete();
		}
	}
}