/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_resolvers_conector;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.client.SiteAgentStatusServiceImpl;
import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.Payload;

import java.util.Set;

public class SiteAgentStatusConnector implements SiteIdResolversConnector {
	private final Set<Class<? extends Body>> applicable = Set.of(AgentPingAck.class);
	private final SiteAgentStatusServiceImpl resolver;

	SiteAgentStatusConnector(SiteAgentStatusServiceImpl siteAgentStatusService) {
		this.resolver = siteAgentStatusService;
	}

	@Override
	public Set<Class<? extends Body>> getApplicableClasses() {
		return applicable;
	}

	@Override
	public SiteExternalId getSiteId(Payload<?> payload) {
		return resolver.getSiteId(new CorrelationId(payload.header.messageCorrelationId));
	}
}
