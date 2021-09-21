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

@Command(name = "community",
		description = "Using to handle Communities.",
		subcommands = {
				CommunityCommand.List.class,
				CommunityCommand.Show.class,
				CommunityAllocationCommand.class
		})
public class CommunityCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Community"));
	}

	@Command(name = "list",
			description = "Returns complete information about all communities including its allocations")
	static class List extends FurmsCommand {

		@Override
		protected void executeCommand() {
			LOG.debug("Executing community list");
			furmsClient.get(path("/communities").build());
		}
	}

	@Command(name = "show",
			description = "Returns complete information about community including its allocation")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Community ID to find Community.")
		private String communityId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing community show {}", communityId);
			furmsClient.get(path("/communities/{communityId}")
					.pathParams(communityId)
					.build());
		}
	}
}