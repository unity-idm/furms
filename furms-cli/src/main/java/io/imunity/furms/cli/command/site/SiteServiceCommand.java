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

@Command(name = "service",
		description = "Site services handling.",
		subcommands = {
				SiteServiceCommand.List.class,
				SiteServiceCommand.Show.class
		})
class SiteServiceCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Service"));
	}

	@Command(name = "list",
			description = "Retrieve all services")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to list related Services")
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site service list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/services")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Retrieve a given site service")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site related to Service")
		private String siteId;

		@Parameters(type = String.class,
				description = "Service ID of Service to find")
		private String serviceId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site service show {} {}", siteId, serviceId);
			furmsClient.get(path("/sites/{siteId}/services/{serviceId}")
					.pathParams(siteId, serviceId)
					.build());
		}
	}
}