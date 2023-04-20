/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.events;

import io.imunity.furms.site.api.site_agent.SiteAgentCommunityOperationService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"io.imunity.furms.core.communites", "io.imunity.furms.core.events"})
public class TestEventsContextConfig {
	@MockBean
	private SiteAgentCommunityOperationService siteAgentCommunityOperationService;
}
