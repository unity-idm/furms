/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.client.message_resolvers_conector.SiteIdResolversConnector;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getSitePublishQueueName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class MessageAuthorizerTest {

	@Test
	void shouldValidatePayload(){
		Body body = Mockito.mock(Body.class);
		SiteIdResolversConnector resolversConnector = Mockito.mock(SiteIdResolversConnector.class);
		MessageAuthorizer messageAuthorizer = new MessageAuthorizer(Map.of(body.getClass(), resolversConnector));
		Payload<Body> payload = new Payload<>(new Header("1", "id"), body);

		when(resolversConnector.getSiteId(payload)).thenReturn(new SiteExternalId("fzx"));

		messageAuthorizer.validate(payload, getSitePublishQueueName("fzx"));
	}

	@Test
	void shouldThrowExceptionWhenPayloadIsNotCorrelatedWithParentSite(){
		Body body = Mockito.mock(Body.class);
		SiteIdResolversConnector resolversConnector = Mockito.mock(SiteIdResolversConnector.class);
		MessageAuthorizer messageAuthorizer = new MessageAuthorizer(Map.of(body.getClass(), resolversConnector));
		Payload<Body> payload = new Payload<>(new Header("1", "id"), body);

		when(resolversConnector.getSiteId(payload)).thenReturn(new SiteExternalId("fzx1"));

		String message = assertThrows(IllegalArgumentException.class, () -> messageAuthorizer.validate(payload, getSitePublishQueueName("fzx")))
			.getMessage();
		assertEquals(String.format("Message doesn't belong to site:  %s", payload), message);
	}

	@Test
	void shouldThrowExceptionWhenResolverIsNotProvided(){
		Body body = Mockito.mock(Body.class);
		SiteIdResolversConnector resolversConnector = Mockito.mock(SiteIdResolversConnector.class);
		MessageAuthorizer messageAuthorizer = new MessageAuthorizer(Map.of(Body.class, resolversConnector));
		Payload<Body> payload = new Payload<>(new Header("1", "id"), body);

		when(resolversConnector.getSiteId(payload)).thenReturn(new SiteExternalId("fzx"));

		String message = assertThrows(IllegalArgumentException.class, () -> messageAuthorizer.validate(payload, getSitePublishQueueName("fzx")))
			.getMessage();
		assertEquals("This shouldn't happened - no MessageAuthorizer fit to payload", message);
	}
}