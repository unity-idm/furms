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

@Command(name = "allocations",
		subcommands = {
				SiteAllocationsCommand.List.class,
				SiteAllocationsCommand.Show.class
		})
class SiteAllocationsCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Allocations"));
	}

	@Command(name = "list",
			description = "Retrieve all project allocations at the site as assigned by the site")
	static class List extends FurmsCommand {

		@Parameters(type = String.class)
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing site allocations list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/siteAllocations")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Retrieve given project allocations at the site as assigned by the site")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class)
		private String siteId;

		@Parameters(type = String.class)
		private String projectId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing site allocations show {} {}", siteId, projectId);
			furmsClient.get(path("/sites/{siteId}/siteAllocations/{projectId}")
					.pathParams(siteId, projectId)
					.build());
		}
	}

}
