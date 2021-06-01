/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.client.message_resolvers_conector.SiteIdResolversConnector;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getSiteId;

@Component
class MessageValidator {
	private final Map<Class<? extends Body>, SiteIdResolversConnector> messageAuthorizers;

	MessageValidator(Map<Class<? extends Body>, SiteIdResolversConnector> messageAuthorizers) {
		this.messageAuthorizers = messageAuthorizers;
	}

	void validate(Payload<?> payload, String queueName){
		SiteIdResolversConnector siteIdResolversConnector = messageAuthorizers.get(payload.body.getClass());
		if(siteIdResolversConnector == null)
			throw new IllegalArgumentException("This shouldn't happened - no MessageAuthorizer fit to payload");
		SiteExternalId siteId = siteIdResolversConnector.getSiteId(payload);
		String siteExternalId = getSiteId(queueName);
		if(!siteExternalId.equals(siteId.id))
			throw new IllegalArgumentException(String.format("Error correlation id %s doesn't belong to %s", payload.header.messageCorrelationId, siteExternalId));
	}
}
