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

@Command(name = "credit",
		description = "Site credits control.",
		subcommands = {
				SiteCreditCommand.List.class,
				SiteCreditCommand.Show.class
		})
class SiteCreditCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Credit"));
	}

	@Command(name = "list",
			description = "Retrieve all resource credits of a site.")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of site to list related Site Credits")
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site credit list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/credits")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Retrieve details of a given resource credit.")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of site related to Site Credit")
		private String siteId;

		@Parameters(type = String.class,
				description = "Credit ID of Site Credit to find")
		private String creditId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site credit show {} {}", siteId, creditId);
			furmsClient.get(path("/sites/{siteId}/credits/{creditId}")
					.pathParams(siteId, creditId)
					.build());
		}
	}

}
