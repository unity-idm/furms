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

@Command(name = "group",
		subcommands = {
				GroupCommand.List.class,
				GroupCommand.Show.class,
				GroupCommand.Delete.class,
				GroupCommand.Create.class,
				GroupCommand.Update.class,
		})
public class GroupCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Group"));
	}

	@Command(name = "list",
			description = "Returns information about all groups in a community.")
	static class List extends FurmsCommand {

		@Parameters(type = String.class)
		private String communityId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing group list {}", communityId);
			furmsClient.get(path("/communities/{communityId}/groups")
					.pathParams(communityId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Returns complete information about a group, including membership information.")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class)
		private String communityId;

		@Parameters(type = String.class)
		private String groupId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing group show {} {}", communityId, groupId);
			furmsClient.get(path("/communities/{communityId}/groups/{groupId}")
					.pathParams(communityId, groupId)
					.build());
		}
	}

	@Command(name = "delete",
			description = "Removes a group from a community.")
	static class Delete extends FurmsCommand {

		@Parameters(type = String.class)
		private String communityId;

		@Parameters(type = String.class)
		private String groupId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing group delete {} {}", communityId, groupId);
			furmsClient.delete(path("/communities/{communityId}/groups/{groupId}")
					.pathParams(communityId, groupId)
					.build());
		}
	}

	@Command(name = "create",
			description = "Creates a new group in a community.")
	static class Create extends FurmsCommand {

		@Parameters(type = String.class)
		private String communityId;

		@Option(names = {"--name"},
				description = "group name",
				required = true)
		private String name;

		@Option(names = {"--description"},
				description = "description attached to group",
				required = true)
		private String description;

		@Override
		protected void executeCommand() throws Exception {
			LOG.info("Executing group create {} {} {}", communityId, name, description);
			furmsClient.post(path("/communities/{communityId}/groups")
					.pathParams(communityId)
					.body(toJson(new GroupRequestJson(name, description)))
					.build());
		}
	}

	@Command(name = "update",
			description = "Update mutable elements of a group.")
	static class Update extends FurmsCommand {

		@Parameters(type = String.class)
		private String communityId;

		@Parameters(type = String.class)
		private String groupId;

		@Option(names = {"--name"},
				description = "new group name",
				required = true)
		private String name;

		@Option(names = {"--description"},
				description = "new description attached to group",
				required = true)
		private String description;

		@Override
		protected void executeCommand() throws Exception {
			LOG.info("Executing group update {} {} {} {}", communityId, groupId, name, description);
			furmsClient.put(path("/communities/{communityId}/groups/{groupId}")
					.pathParams(communityId, groupId)
					.body(toJson(new GroupRequestJson(name, description)))
					.build());
		}

	}


}
