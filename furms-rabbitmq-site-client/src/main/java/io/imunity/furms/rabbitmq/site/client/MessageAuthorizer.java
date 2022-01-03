/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.rabbitmq.site.client.message_resolvers_conector.DefaultSiteIdResolversConnector;
import io.imunity.furms.rabbitmq.site.client.message_resolvers_conector.SiteIdGetter;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getSiteId;

@Component
public class MessageAuthorizer {
	private final Map<Class<? extends Body>, SiteIdGetter> siteIdGetterMap;
	private final DefaultSiteIdResolversConnector defaultSiteIdResolversConnector;

	MessageAuthorizer(Map<Class<? extends Body>, SiteIdGetter> siteIdGetterMap, DefaultSiteIdResolversConnector defaultSiteIdResolversConnector) {
		this.siteIdGetterMap = siteIdGetterMap;
		this.defaultSiteIdResolversConnector = defaultSiteIdResolversConnector;
	}

	void validate(Payload<?> payload, String queueName){
		SiteIdGetter siteIdResolversConnector = siteIdGetterMap.getOrDefault(payload.body.getClass(), defaultSiteIdResolversConnector);
		siteIdResolversConnector.getSiteId(payload)
			.filter(siteExternalId -> getSiteId(queueName).equals(siteExternalId.id))
			.orElseThrow(() -> new InvalidSiteIdException(String.format("Message doesn't belong to site:  %s", payload)));
	}
}
