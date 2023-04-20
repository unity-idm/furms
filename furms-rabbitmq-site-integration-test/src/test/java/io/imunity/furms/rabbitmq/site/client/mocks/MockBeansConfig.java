/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.mocks;

import io.imunity.furms.rabbitmq.site.client.MessageAuthorizer;
import io.imunity.furms.site.api.AgentPendingMessageSiteService;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.message_remover.AgentSSHKeyRemover;
import io.imunity.furms.site.api.message_remover.ProjectAllocationInstallationRemover;
import io.imunity.furms.site.api.message_remover.ProjectInstallationRemover;
import io.imunity.furms.site.api.message_remover.ResourceAccessRemover;
import io.imunity.furms.site.api.message_remover.UserProjectAddRemover;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationChunkSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ResourceUsageSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ResourceUsageUpdater;
import io.imunity.furms.site.api.status_updater.ProjectAllocationInstallationStatusUpdater;
import io.imunity.furms.site.api.status_updater.ProjectInstallationStatusUpdater;
import io.imunity.furms.site.api.status_updater.SSHKeyOperationStatusUpdater;
import io.imunity.furms.site.api.status_updater.UserAllocationStatusUpdater;
import io.imunity.furms.site.api.status_updater.UserOperationStatusUpdater;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MockBeansConfig {
	@MockBean
	private ProjectInstallationStatusUpdater projectInstallationService;
	@MockBean
	private ProjectAllocationInstallationStatusUpdater projectAllocationInstallationStatusUpdater;
	@MockBean
	private SiteExternalIdsResolver siteExternalIdsResolver;
	@MockBean
	private SSHKeyOperationStatusUpdater sshKeyOperationService;
	@MockBean
	private UserOperationStatusUpdater userOperationStatusUpdater;
	@MockBean
	private UserAllocationStatusUpdater userAllocationStatusUpdater;
	@MockBean
	private ResourceUsageUpdater resourceUsageUpdater;

	@MockBean
	private MessageAuthorizer messageAuthorizer;

	@MockBean
	private AgentPendingMessageSiteService projectAllocationInstallationSiteIdResolver;
	@MockBean
	private AgentSSHKeyRemover projectInstallationSiteIdResolver;
	@MockBean
	private ProjectAllocationInstallationRemover projectUpdateSiteIdResolver;
	@MockBean
	private ProjectInstallationRemover sshKeySiteIdResolver;
	@MockBean
	private ResourceAccessRemover userAdditionSiteIdResolver;
	@MockBean
	private UserProjectAddRemover userAllocationGrantSiteIdResolver;
	@MockBean
	private ProjectAllocationChunkSiteIdResolver projectAllocationChunkSiteIdResolver;
	@MockBean
	private ResourceUsageSiteIdResolver resourceUsageSiteIdResolver;
	@MockBean
	private SiteAgentPolicyDocumentReceiverMock siteAgentPolicyDocumentReceiverMock;
	@MockBean
	private SiteAgentCommunityReceiverMock siteAgentCommunityReceiverMock;
	@MockBean
	private SiteAgentMessageErrorInfoReceiverMock siteAgentMessageErrorInfoReceiverMock;
}
