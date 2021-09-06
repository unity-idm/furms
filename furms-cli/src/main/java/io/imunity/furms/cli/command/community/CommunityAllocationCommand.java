/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.community;

import io.imunity.furms.cli.command.FurmsCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static io.imunity.furms.cli.client.FurmsClientRequest.path;
import static io.imunity.furms.cli.command.CommandUtils.createEmptyCommandMessage;
import static org.apache.http.util.TextUtils.isBlank;

@Command(name = "allocation",
		subcommands = {
				CommunityAllocationCommand.List.class,
				CommunityAllocationCommand.Show.class,
				CommunityAllocationCommand.Create.class
		})
class CommunityAllocationCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Community Allocation"));
	}

	@Command(name = "list",
			description = "Retrieve all allocations of a community.")
	static class List extends FurmsCommand {

		@Parameters(type = String.class)
		private String communityId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing community allocation list {}", communityId);
			furmsClient.get(path("/communities/{communityId}/allocations")
					.pathParams(communityId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Retrieve a particular allocation of a community.")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class)
		private String communityId;

		@Parameters(type = String.class)
		private String allocationId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing community allocation show {} {}", communityId, allocationId);
			furmsClient.get(path("/communities/{communityId}/allocations/{communityAllocationId}")
					.pathParams(communityId, allocationId)
					.build());
		}
	}

	@Command(name = "create",
			description = "Create a new allocation for a community.")
	static class Create extends FurmsCommand {

		@Parameters(type = String.class)
		private String communityId;

		@Option(names = {"--file"},
				description = "path to json file that defines allocation",
				required = true)
		private String definitionFilePath;

		@Override
		protected void executeCommand() {
			if (isBlank(communityId)) {
				throw new IllegalArgumentException("Please define communityId as program argument or type --help.");
			}
			LOG.info("Executing community allocation create {}, {}", communityId, definitionFilePath);
			furmsClient.post(path("/communities/{communityId}/allocations")
					.pathParams(communityId)
					.body(readFile(definitionFilePath))
					.build());
		}
	}
}