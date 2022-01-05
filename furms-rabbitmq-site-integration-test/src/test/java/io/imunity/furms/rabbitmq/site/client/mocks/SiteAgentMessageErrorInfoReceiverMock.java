/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.mocks;

import io.imunity.furms.rabbitmq.site.models.AgentMessageErrorInfo;

public interface SiteAgentMessageErrorInfoReceiverMock {
	void process(AgentMessageErrorInfo message);
}
