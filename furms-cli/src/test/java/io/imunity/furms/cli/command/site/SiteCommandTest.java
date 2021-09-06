/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.command.site;

import io.imunity.furms.cli.CLICommandsTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;

class SiteCommandTest extends CLICommandsTest {

	@Test
	void shouldCallForSitesList() {
		runCLI("site", "list");

		server.verify(getRequestedFor(restPath("/sites")));
	}

	@Test
	void shouldCallForSpecificSite() {
		final String siteId = "siteId";
		runCLI("site", "show", siteId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}", siteId)));
	}

	@Test
	void shouldCallForUsageRecordsForSpecificSite() {
		final String siteId = "siteId";
		final String projectId = "projectId";
		final String from = "from";
		final String until = "until";
		runCLI("site", "usageRecords", siteId, projectId, "--from", from, "--until", until);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/usageRecords/{projectId}.*", siteId, projectId))
				.withQueryParam("from", equalTo(from))
				.withQueryParam("until", equalTo(until)));
	}

	@Test
	void shouldCallForCumulativeResourcesConsumptionForSpecificSite() {
		final String siteId = "siteId";
		final String projectId = "projectId";
		runCLI("site", "cumulativeResourcesConsumption", siteId, projectId);

		server.verify(getRequestedFor(restPath("/sites/{siteId}/cumulativeResourcesConsumption/{projectId}", siteId, projectId)));
	}
}