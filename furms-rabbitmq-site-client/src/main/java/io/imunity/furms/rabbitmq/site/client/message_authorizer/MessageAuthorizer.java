/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_authorizer;

import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.Payload;

public interface MessageAuthorizer {
	boolean isApplicable(Class<? extends Body> clazz);
	boolean isValidate(String queueName, Payload<?> payload);
}
