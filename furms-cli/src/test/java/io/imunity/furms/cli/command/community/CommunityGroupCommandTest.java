/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.community;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;

class CommunityGroupCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForGroupList() {
		final String communityId = "communityId";
		runCLI("community", "group", "list", communityId);

		server.verify(getRequestedFor(restPath("/communities/{communityId}/groups", communityId)));
	}

	@Test
	void shouldCallForSpecificGroup() {
		final String communityId = "communityId";
		final String groupId = "groupId";
		runCLI("community", "group", "show", communityId, groupId);

		server.verify(getRequestedFor(restPath("/communities/{communityId}/groups/{groupId}",
				communityId, groupId)));
	}

	@Test
	void shouldCallForDeleteSpecificGroup() {
		final String communityId = "communityId";
		final String groupId = "groupId";
		runCLI("community", "group", "delete", communityId, groupId);

		server.verify(deleteRequestedFor(restPath("/communities/{communityId}/groups/{groupId}",
				communityId, groupId)));
	}

	@Test
	void shouldCallForCreateGroup() throws JsonProcessingException {
		final String communityId = "communityId";
		final String groupName = "groupName";
		final String groupDescription = "groupDescription";
		runCLI("community", "group", "create", communityId, "--name", groupName, "--description", groupDescription);

		server.verify(postRequestedFor(restPath("/communities/{communityId}/groups", communityId))
				.withRequestBody(equalToJson(objectMapper.writeValueAsString(new GroupRequestJson(groupName, groupDescription)))));
	}

	@Test
	void shouldCallForUpdateGroup() throws JsonProcessingException {
		final String communityId = "communityId";
		final String groupId = "groupId";
		final String groupName = "groupName";
		final String groupDescription = "groupDescription";
		runCLI("community", "group", "update", communityId, groupId, "--name", groupName, "--description", groupDescription);

		server.verify(putRequestedFor(restPath("/communities/{communityId}/groups/{groupId}", communityId, groupId))
				.withRequestBody(equalToJson(objectMapper.writeValueAsString(new GroupRequestJson(groupName, groupDescription)))));
	}

}