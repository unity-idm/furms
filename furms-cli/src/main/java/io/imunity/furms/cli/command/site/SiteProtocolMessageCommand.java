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

@Command(name = "protocolMsg",
		description = "Handling of the communication with site agent.",
		subcommands = {
				SiteProtocolMessageCommand.List.class,
				SiteProtocolMessageCommand.Delete.class,
				SiteProtocolMessageCommand.Retry.class,
		})
class SiteProtocolMessageCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site Protocol Message"));
	}

	@Command(name = "list",
			description = "Returns all pending site's requests, which were sent to the site agent.")
	static class List extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to list related Site Protocol Messages")
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site protocolMsg list {}", siteId);
			furmsClient.get(path("/sites/{siteId}/protocolMessages")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "delete",
			description = "Removes a pending protocol request. It won't be possible to retry the removed request, "
					+ "FURMS will assume it has timed out.")
	static class Delete extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site related Site Protocol Message")
		private String siteId;

		@Parameters(type = String.class,
				description = "Message ID of Site Protocol Message to delete")
		private String messageId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site protocolMsg delete {} {}", siteId, messageId);
			furmsClient.delete(path("/sites/{siteId}/protocolMessages/{messageId}")
					.pathParams(siteId, messageId)
					.build());
		}
	}

	@Command(name = "retry",
			description = "Retry sending a request to a site agent.")
	static class Retry extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site related Site Protocol Message")
		private String siteId;

		@Parameters(type = String.class,
				description = "Message ID of Site Protocol Message to Retry")
		private String messageId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site protocolMsg retry {} {}", siteId, messageId);
			furmsClient.post(path("/sites/{siteId}/protocolMessages/{messageId}")
					.pathParams(siteId, messageId)
					.build());
		}
	}

}
