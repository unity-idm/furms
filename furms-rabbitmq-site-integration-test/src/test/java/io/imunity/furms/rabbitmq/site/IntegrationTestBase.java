/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site;

import io.imunity.furms.rabbitmq.site.client.SiteAgentListenerConnector;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentMock.MOCK_FURMS_PUB;
import static io.imunity.furms.rabbitmq.site.client.mocks.SiteAgentMock.MOCK_SITE_PUB;

@SpringBootTest
public class IntegrationTestBase {
	protected final long DEFAULT_RECEIVE_TIMEOUT_MILIS = 10000;

	@Autowired
	protected SiteAgentListenerConnector siteAgentListenerConnector;

	@Autowired
	protected RabbitTemplate rabbitTemplate;

	@Autowired
	protected RabbitAdmin rabbitAdmin;

	private static boolean isSiteAgentRegistered;

	@BeforeEach
	void init(){
		rabbitAdmin.purgeQueue(MOCK_FURMS_PUB);
		rabbitAdmin.purgeQueue(MOCK_SITE_PUB);
		if (!isSiteAgentRegistered) {
			siteAgentListenerConnector.connectListenerToQueue(MOCK_FURMS_PUB);
			siteAgentListenerConnector.connectListenerToQueue(MOCK_SITE_PUB);
			isSiteAgentRegistered = true;
		}
	}
}
