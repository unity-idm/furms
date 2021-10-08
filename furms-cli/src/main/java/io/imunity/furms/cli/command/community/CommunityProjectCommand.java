/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.community;

import io.imunity.furms.cli.command.FurmsCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import static io.imunity.furms.cli.client.FurmsClientRequest.path;
import static io.imunity.furms.cli.command.CommandUtils.createEmptyCommandMessage;

@Command(name = "project",
		description = "Community projects control.",
		subcommands = {
				CommunityProjectCommand.List.class
		})
public class CommunityProjectCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Community Project"));
	}

	@Command(name = "list",
			description = "Returns complete information about all projects related to community")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Community ID to find Projects.")
		private String communityId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing community project list {}", communityId);
			furmsClient.get(path("/communities/{communityId}/projects")
					.pathParams(communityId)
					.build());
		}
	}
}
