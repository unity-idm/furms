/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_authorizer;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.message_resolver.UserAdditionMessageResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class UserAdditionMessageAuthorizer implements MessageAuthorizer{
	private final Set<Class<? extends Body>> applicable = Set.of(UserProjectAddRequestAck.class, UserProjectAddResult.class, UserAllocationBlockAccessRequestAck.class, UserAllocationBlockAccessResult.class);
	private final UserAdditionMessageResolver resolver;

	UserAdditionMessageAuthorizer(UserAdditionMessageResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public boolean isApplicable(Class<? extends Body> clazz) {
		return applicable.contains(clazz);
	}

	@Override
	public boolean isValidate(String siteId, Payload<?> payload) {
		String messageCorrelationId = payload.header.messageCorrelationId;
		return resolver.isMessageCorrelated(new CorrelationId(messageCorrelationId), new SiteExternalId(siteId));
	}
}
