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

@Command(name = "project",
		subcommands = {
			ProjectCommand.List.class,
			ProjectCommand.Show.class,
			ProjectCommand.Delete.class,
			ProjectCommand.Create.class,
			ProjectCommand.Update.class,
			ProjectAllocationCommand.class
		})
public class ProjectCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Project"));
	}

	@Command(name = "list",
			description = "Returns complete information about all projects including its allocations.")
	static class List extends FurmsCommand {

		@Override
		protected void executeCommand() {
			LOG.debug("Executing project list");
			furmsClient.get(path("/projects")
					.build());
		}
	}

	@Command(name = "show",
			description = "Returns complete information about project including its allocations and its members.")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class)
		private String projectId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing project show {}", projectId);
			furmsClient.get(path("/projects/{projectId}")
					.pathParams(projectId)
					.build());
		}
	}

	@Command(name = "delete",
			description = "Removes project from community.")
	static class Delete extends FurmsCommand {

		@Parameters(type = String.class)
		private String projectId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing project delete {}", projectId);
			furmsClient.delete(path("/projects/{projectId}")
					.pathParams(projectId)
					.build());
		}
	}

	@Command(name = "create",
			description = "Creates project under particular community.")
	static class Create extends FurmsCommand {

		@Option(names = {"--file"},
				description = "path to json file that defines project",
				required = true)
		private String definitionFilePath;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing project create {}", definitionFilePath);
			furmsClient.post(path("/projects")
					.body(readFile(definitionFilePath))
					.build());
		}
	}

	@Command(name = "update",
			description = "Update particular project.")
	static class Update extends FurmsCommand {

		@Parameters(type = String.class)
		private String projectId;

		@Option(names = {"--file"},
				description = "path to json file that defines project",
				required = true)
		private String definitionFilePath;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing project update {} {}", projectId, definitionFilePath);
			furmsClient.put(path("/projects/{projectId}")
					.pathParams(projectId)
					.body(readFile(definitionFilePath))
					.build());
		}
	}

}
