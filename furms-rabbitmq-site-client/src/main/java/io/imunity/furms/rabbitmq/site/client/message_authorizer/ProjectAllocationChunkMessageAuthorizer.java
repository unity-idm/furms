/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_authorizer;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationResult;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationChunkMessageResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class ProjectAllocationChunkMessageAuthorizer implements MessageAuthorizer{
	private final Set<Class<? extends Body>> applicable = Set.of(AgentProjectAllocationInstallationResult.class);
	private final ProjectAllocationChunkMessageResolver resolver;

	ProjectAllocationChunkMessageAuthorizer(ProjectAllocationChunkMessageResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public boolean isApplicable(Class<? extends Body> clazz) {
		return applicable.contains(clazz);
	}

	@Override
	public boolean isValidate(String siteId, Payload<?> payload) {
		AgentProjectAllocationInstallationResult body = (AgentProjectAllocationInstallationResult)payload.body;
		return resolver.isMessageCorrelated(body.allocationIdentifier, new SiteExternalId(siteId));
	}
}
