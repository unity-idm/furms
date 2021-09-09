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

		@Parameters(type = String.class)
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing site policy list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/policies")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "show",
			description = "Retrieve a given site policy")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class)
		private String siteId;

		@Parameters(type = String.class)
		private String policyId;

		@Override
		protected void executeCommand() {
			LOG.info("Executing site show {} {}", siteId, policyId);
			furmsClient.get(path("/sites/{siteId}/policies/{policyId}")
					.pathParams(siteId, policyId)
					.build());
		}
	}
}