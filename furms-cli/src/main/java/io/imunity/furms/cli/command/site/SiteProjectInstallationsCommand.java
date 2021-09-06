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

@Command(name = "projectInstallations",
		subcommands = {
				SiteProjectInstallationsCommand.List.class
		})
class SiteProjectInstallationsCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Project Installations"));
	}

	@Command(name = "list",
			description = "Returns list of projects that should be installed on the site with "
					+ "details of installation status")
	static class List extends FurmsCommand {

		@Parameters(type = String.class)
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing site credit projectInstallations list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/projectInstallations")
					.pathParams(siteId)
					.build());
		}
	}
}
