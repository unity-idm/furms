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

@Command(name = "resourceType",
		description = "Using to handle Site Resource Types",
		subcommands = {
				SiteResourceTypeCommand.List.class,
				SiteResourceTypeCommand.Show.class
		})
class SiteResourceTypeCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Resource Type"));
	}

	@Command(name = "list",
			description = "Retrieve all resource types.")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to list related Resource Types")
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site resourceType list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/resourceTypes")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Retrieve a given resource type.")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site related to Resource Type")
		private String siteId;

		@Parameters(type = String.class,
				description = "Resource Type ID of Resource Type to find")
		private String resourceTypeId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site resourceType show {} {}", siteId, resourceTypeId);
			furmsClient.get(path("/sites/{siteId}/resourceTypes/{resourceTypeId}")
					.pathParams(siteId, resourceTypeId)
					.build());
		}
	}

}
