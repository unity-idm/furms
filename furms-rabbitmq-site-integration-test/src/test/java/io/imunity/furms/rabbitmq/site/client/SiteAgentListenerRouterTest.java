/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.site.api.AgentPendingMessageSiteService;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.site.api.status_updater.UserOperationStatusUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class SiteAgentListenerRouterTest extends IntegrationTestBase {

	@Autowired
	private SiteAgentUserService siteAgentUserService;
	@Autowired
	private AgentPendingMessageSiteService agentPendingMessageSiteService;
	@Autowired
	private UserOperationStatusUpdater userOperationStatusUpdater;

	@Test
	void shouldHandlePendingUserAdditionMessage() {
		CorrelationId correlationId = CorrelationId.randomID();
		UserAddition userAddition = UserAddition.builder()
			.id("id")
			.siteId(new SiteId("id", new SiteExternalId("mock")))
			.projectId("projectId")
			.correlationId(correlationId)
			.build();
		FURMSUser user = FURMSUser.builder()
			.fenixUserId(new FenixUserId("id"))
			.email("email")
			.build();
		siteAgentUserService.addUser(userAddition, new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of()));

		verify(agentPendingMessageSiteService, timeout(10000)).setAsAcknowledged(correlationId);
		verify(agentPendingMessageSiteService, timeout(10000)).delete(correlationId);
	}
}
