/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_remover;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_remover.SiteAgentPendingMessageRemoverConnector;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
class SiteAgentPendingMessageRemoverConnectorImpl implements SiteAgentPendingMessageRemoverConnector {

	private final List<PendingMessageRemoveStrategy> strategies;
	private final JacksonJsonParser jacksonJsonParser = new JacksonJsonParser();

	SiteAgentPendingMessageRemoverConnectorImpl(List<PendingMessageRemoveStrategy> strategies) {
		this.strategies = strategies;
	}

	@Override
	public void remove(CorrelationId correlationId, String json){
		String type = ((Map<String, ?>) jacksonJsonParser.parseMap(json).get("body")).keySet().stream()
			.findAny()
			.orElse("");
		strategies.stream()
			.filter(x -> x.isApplicable(type))
			.forEach(x -> x.remove(correlationId));
	}
}
