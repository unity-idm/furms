/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.config;

import io.imunity.furms.rabbitmq.site.client.SiteAgentListenerConnector;
import io.imunity.furms.rabbitmq.site.client.SiteAgentStatusServiceImpl;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationChunkSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ProjectDeallocationSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ProjectUpdateSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ResourceUsageSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.SSHKeySiteIdResolver;
import io.imunity.furms.site.api.message_resolver.UserAccountStatusUpdateSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.UserAdditionSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.UserAllocationGrantSiteIdResolver;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootApplication(scanBasePackageClasses = ConnectionInitializer.class, scanBasePackages = "io.imunity.furms.rabbitmq.site.client.message_resolvers_conector")
class TestSpringContextConfig {
	@MockBean
	private ProjectAllocationInstallationSiteIdResolver projectAllocationInstallationSiteIdResolver;
	@MockBean
	private ProjectInstallationSiteIdResolver projectInstallationSiteIdResolver;
	@MockBean
	private ProjectUpdateSiteIdResolver projectUpdateSiteIdResolver;
	@MockBean
	private SSHKeySiteIdResolver sshKeySiteIdResolver;
	@MockBean
	private UserAdditionSiteIdResolver userAdditionSiteIdResolver;
	@MockBean
	private UserAllocationGrantSiteIdResolver userAllocationGrantSiteIdResolver;
	@MockBean
	private ProjectAllocationChunkSiteIdResolver projectAllocationChunkSiteIdResolver;
	@MockBean
	private ProjectDeallocationSiteIdResolver projectDeallocationSiteIdResolver;
	@MockBean
	private ResourceUsageSiteIdResolver resourceUsageSiteIdResolver;
	@MockBean
	private SiteExternalIdsResolver siteExternalIdsResolver;
	@MockBean
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@MockBean
	private SiteAgentStatusServiceImpl siteAgentStatusServiceImpl;
	@MockBean
	private UserAccountStatusUpdateSiteIdResolver userAccountStatusUpdateSiteIdResolver;
}
