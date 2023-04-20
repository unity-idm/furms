/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.communities.CommunityInstallation;
import io.imunity.furms.domain.communities.CommunityUpdate;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.IntegrationTestBase;
import io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentCommunityReceiverMock;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityUpdateRequest;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentCommunityOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SiteAgentCommunityServiceTest extends IntegrationTestBase {

	private SiteAgentCommunityOperationService service;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private SiteAgentCommunityReceiverMock receiverMock;

	@BeforeEach
	void setUp()
	{
		SiteExternalIdsResolver siteExternalIdsResolver = mock(SiteExternalIdsResolver.class);
		service = new SiteAgentCommunityOperationServiceImpl(rabbitTemplate, siteExternalIdsResolver);
		when(siteExternalIdsResolver.findAllIds()).thenReturn(Set.of(new SiteExternalId("mock")));
	}

	@Test
	void shouldInstallCommunity() {
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		CommunityInstallation community = new CommunityInstallation(communityId, "communityName1", "description");

		service.installCommunity(community);

		verify(receiverMock, timeout(10000)).process(
			new AgentCommunityInstallationRequest(communityId.id.toString(), "communityName1", "description")
		);
	}

	@Test
	void shouldUpdateCommunity() {
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		CommunityUpdate community = new CommunityUpdate(communityId, "communityName1", "description");


		service.updateCommunity(community);

		verify(receiverMock, timeout(10000)).process(
			new AgentCommunityUpdateRequest(communityId.id.toString(), "communityName1", "description")
		);
	}

	@Test
	void shouldRemoveCommunity() {
		CommunityId communityId = new CommunityId(UUID.randomUUID());

		service.removeCommunity(communityId);

		verify(receiverMock, timeout(10000)).process(
			new AgentCommunityRemovalRequest(communityId.id.toString())
		);
	}
}
