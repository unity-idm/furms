/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.project;

import io.imunity.furms.cli.command.FurmsCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static io.imunity.furms.cli.client.FurmsClientRequest.path;
import static io.imunity.furms.cli.command.CommandUtils.createEmptyCommandMessage;

@Command(name = "allocation",
		description = "Project allocations control",
		subcommands = {
			ProjectAllocationCommand.List.class,
			ProjectAllocationCommand.Show.class,
			ProjectAllocationCommand.Create.class
		})
class ProjectAllocationCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Project Allocation"));
	}

	@Command(name = "list",
			description = "Retrieve all project's allocations information")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Project ID to list related Project Allocations")
		private String projectId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing project allocation list {}", projectId);
			furmsClient.get(path("/projects/{projectId}/allocations")
					.pathParams(projectId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Retrieve project's allocation information")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Project ID of Project related to Allocation")
		private String projectId;

		@Parameters(type = String.class,
				description = "Project Allocation ID to find Allocation")
		private String allocationId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing project allocation get {} {}", projectId, allocationId);
			furmsClient.get(path("/projects/{projectId}/allocations/{projectAllocationId}")
					.pathParams(projectId, allocationId)
					.build());
		}
	}

	@Command(name = "create",
			description = "Allocate resources to a project.")
	static class Create extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Project ID to attach Allocation")
		private String projectId;

		@Option(names = {"--file"},
				description = "Path to json file that defines allocation",
				required = true)
		private String definitionFilePath;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing project allocation create {}, {}", projectId, definitionFilePath);
			furmsClient.post(path("/projects/{projectId}/allocations")
					.pathParams(projectId)
					.body(readFile(definitionFilePath))
					.build());
		}
	}

}
