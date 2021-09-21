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

@Command(name = "furmsAllocations",
		description = "Using to handle FURMS Allocations",
		subcommands = {
				SiteFurmsAllocationsCommand.List.class,
				SiteFurmsAllocationsCommand.Show.class
		})
class SiteFurmsAllocationsCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Furms Allocations"));
	}

	@Command(name = "list",
			description = "Retrieve all project allocations at the site as assigned in FURMS")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to find FURMS Allocations")
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site furmsAllocations list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/furmsAllocations")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Retrieve given project allocations at the site as assigned in FURMS")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site related to Project")
		private String siteId;

		@Parameters(type = String.class,
				description = "Project ID of Project to find FURMS Allocation")
		private String projectId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site furmsAllocations show {} {}", siteId, projectId);
			furmsClient.get(path("/sites/{siteId}/furmsAllocations/{projectId}")
					.pathParams(siteId, projectId)
					.build());
		}
	}

}
