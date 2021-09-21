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

@Command(name = "acceptance",
		description = "Using to handle Site Policy Acceptances",
		subcommands = {
				SitePolicyAcceptanceCommand.List.class,
				SitePolicyAcceptanceCommand.Ack.class
		})
class SitePolicyAcceptanceCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Policy Acceptance"));
	}

	@Command(name = "list",
			description = "Returns list of all site's users with policies and status whether given policy "
					+ "is accepcted or not.")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to list related Policy Acceptances")
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site policy acceptance list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/policyAcceptances")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "ack",
			description = "Performs operation of accepting the policy on behalf of given user.")
	static class Ack extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to ACK related Policy Acceptance")
		private String siteId;

		@Parameters(type = String.class,
				description = "Policy ID of Policy to ACK")
		private String policyId;

		@Parameters(type = String.class,
				description = "Fenix User ID of Acceptor")
		private String fenixUserId;

		@Parameters(type = String.class,
				description = "Status to set (ACCEPTED, NOT_ACCEPTED, ACCEPTED_FORMER_REVISION)")
		private String status;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site policy acceptance ack {} {} {} {}", siteId, policyId, fenixUserId, status);
			furmsClient.post(path("/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}/{status}")
					.pathParams(siteId, policyId, fenixUserId, status)
					.build());
		}
	}

}
