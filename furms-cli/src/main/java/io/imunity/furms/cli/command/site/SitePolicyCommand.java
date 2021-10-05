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

@Command(name = "policy",
		description = "Site policies control.",
		subcommands = {
				SitePolicyCommand.List.class,
				SitePolicyCommand.Show.class,
				SitePolicyAcceptanceCommand.class
		})
class SitePolicyCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Policy"));
	}

	@Command(name = "list",
			description = "Retrieve site policies")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to list Policies")
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site policy list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/policies")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Retrieve a given site policy")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site related to Policy")
		private String siteId;

		@Parameters(type = String.class,
				description = "Policy ID of Policy to find")
		private String policyId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site show {} {}", siteId, policyId);
			furmsClient.get(path("/sites/{siteId}/policies/{policyId}")
					.pathParams(siteId, policyId)
					.build());
		}
	}
}