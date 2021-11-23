/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_resolvers_conector;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.site.api.AgentPendingMessageSiteService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DefaultSiteIdResolversConnector implements SiteIdGetter {
	private final AgentPendingMessageSiteService resolver;

	DefaultSiteIdResolversConnector(AgentPendingMessageSiteService resolver) {
		this.resolver = resolver;
	}

	@Override
	public Optional<SiteExternalId> getSiteId(Payload<?> payload) {
		String messageCorrelationId = payload.header.messageCorrelationId;
		return resolver.find(new CorrelationId(messageCorrelationId))
			.map(message -> message.siteExternalId);
	}
}
