/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.command.FurmsCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import static io.imunity.furms.cli.client.FurmsClientRequest.path;
import static io.imunity.furms.cli.command.CommandUtils.createEmptyCommandMessage;

@Command(name = "users",
		description = "Using to handle Site Users",
		subcommands = {
				SiteUsersCommand.List.class
		})
class SiteUsersCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Users"));
	}

	@Command(name = "list",
			description = "Returns list of users that have access to the site through at least "
					+ "one of their projects. SSH key of each user is provided as well.")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to list related Users")
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site users list {} {}", siteId);
			furmsClient.get(path("/sites/{siteId}/users")
					.pathParams(siteId)
					.build());
		}
	}

}
