/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.mocks;

import io.imunity.furms.rabbitmq.site.models.AgentCommunityInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityUpdateRequest;

public interface SiteAgentCommunityReceiverMock
{
	void process(AgentCommunityInstallationRequest message);
	void process(AgentCommunityUpdateRequest message);
	void process(AgentCommunityRemovalRequest message);
}
