/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.command.FurmsCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.HashMap;
import java.util.Map;

import static io.imunity.furms.cli.client.FurmsClientRequest.path;
import static io.imunity.furms.cli.command.CommandUtils.createEmptyCommandMessage;

@Command(name = "site",
		description = "Sites control.",
		subcommands = {
				SiteCommand.List.class,
				SiteCommand.Show.class,
				SiteCommand.UsageRecords.class,
				SiteCommand.CumulativeResourcesConsumption.class,
				SitePolicyCommand.class,
				SiteServiceCommand.class,
				SiteCreditCommand.class,
				SiteResourceTypeCommand.class,
				SiteProtocolMessageCommand.class,
				SiteProjectInstallationsCommand.class,
				SiteUsersCommand.class,
				SiteFurmsAllocationsCommand.class,
				SiteAllocationsCommand.class
		})
public class SiteCommand extends FurmsCommand {

	@Override
	protected void executeCommand() {
		LOG.error(createEmptyCommandMessage("Site"));
	}

	@Command(name = "list",
			description = "Returns complete information about all sites including their allocations, "
					+ "resource types, services and policies.")
	static class List extends FurmsCommand {
		@Override
		protected void executeCommand() {
			LOG.debug("Executing site list");
			furmsClient.get(path("/sites")
					.build());
		}
	}

	@Command(name = "show",
			description = "Returns complete information about a site, including its all allocations, "
					+ "resource types, services and policies")
	static class Show extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to find")
		private String siteId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site show {}", siteId);
			furmsClient.get(path("/sites/{siteId}")
					.pathParams(siteId)
					.build());
		}
	}

	@Command(name = "usageRecords",
			description = "Retrieve individual resoruce usage records, stored by FURMS for "
					+ "a given project on a site. Records have per-user granularity. "
					+ "Caller can limit time range of records.")
	static class UsageRecords extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to show usage records")
		private String siteId;

		@Parameters(type = String.class,
				description = "Project ID of Project to show usage records")
		private String projectId;

		@Option(names = {"--from"},
				description = "Start of period to limit the results")
		private String from;

		@Option(names = {"--until"},
				description = "End of period to limit the results")
		private String until;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site usageRecords {} {} {} {}", siteId, projectId, from, until);

			final Map<String, String> queryParams = new HashMap<>();
			queryParams.put("from", from);
			queryParams.put("until", until);

			furmsClient.get(path("/sites/{siteId}/usageRecords/{projectId}")
					.pathParams(siteId, projectId)
					.queryParams(queryParams)
					.build());
		}
	}

	@Command(name = "cumulativeResourcesConsumption",
			description = "Retrieve cumultaive resoruce consumption recorded by FURMS for "
					+ "a given project on a site.")
	static class CumulativeResourcesConsumption extends FurmsCommand {

		@Parameters(type = String.class,
				description = "Site ID of Site to show cumulative resource consumption")
		private String siteId;

		@Parameters(type = String.class,
				description = "Project ID of Project to show cumulative resource consumption")
		private String projectId;

		@Override
		protected void executeCommand() {
			LOG.debug("Executing site cumulativeResourcesConsumption {} {}", siteId, projectId);
			furmsClient.get(path("/sites/{siteId}/cumulativeResourcesConsumption/{projectId}")
					.pathParams(siteId,projectId)
					.build());
		}
	}
}