/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site;

import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.rabbitmq.site.client.SiteAgentListenerConnector;
import io.imunity.furms.site.api.message_resolver.ResourceAccessMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SiteAgentResourceAccessServiceTest {
	@Autowired
	private SiteAgentResourceAccessService siteAgentResourceAccessService;
	@Autowired
	private SiteAgentListenerConnector siteAgentListenerConnector;
	@Autowired
	private ResourceAccessMessageResolver resourceAccessMessageResolver;

	@BeforeEach
	void init(){
		siteAgentListenerConnector.connectListenerToQueue( "mock-site-pub");
	}

	@Test
	void shouldGrantAccess() {
		CorrelationId correlationId = CorrelationId.randomID();
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("id", "mock"))
			.build();
		siteAgentResourceAccessService.grantAccess(correlationId, grantAccess);

		verify(resourceAccessMessageResolver, timeout(10000)).update(
			correlationId,
			AccessStatus.GRANT_ACKNOWLEDGED,
			null
		);
		verify(resourceAccessMessageResolver, timeout(10000)).update(
			correlationId,
			AccessStatus.GRANTED,
			null
		);
	}

	@Test
	void shouldRevokeAccess() {
		CorrelationId correlationId = CorrelationId.randomID();
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("id", "mock"))
			.build();
		siteAgentResourceAccessService.revokeAccess(correlationId, grantAccess);

		verify(resourceAccessMessageResolver, timeout(10000)).update(
			correlationId,
			AccessStatus.REVOKE_ACKNOWLEDGED,
			null
		);
		verify(resourceAccessMessageResolver, timeout(10000)).update(
			correlationId,
			AccessStatus.REVOKED,
			null
		);
	}
}
