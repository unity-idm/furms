/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_resolvers_conector;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationResult;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationUpdate;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationChunkSiteIdResolver;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
class ProjectAllocationChunkSiteIdResolversConnector implements SiteIdResolversConnector {
	private final Set<Class<? extends Body>> applicable = Set.of(AgentProjectAllocationInstallationResult.class, AgentProjectAllocationUpdate.class);
	private final ProjectAllocationChunkSiteIdResolver resolver;

	ProjectAllocationChunkSiteIdResolversConnector(ProjectAllocationChunkSiteIdResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Set<Class<? extends Body>> getApplicableClasses() {
		return applicable;
	}

	@Override
	public Optional<SiteExternalId> getSiteId(Payload<?> payload) {
		if(payload.body.getClass().equals(AgentProjectAllocationInstallationResult.class)) {
			AgentProjectAllocationInstallationResult body = (AgentProjectAllocationInstallationResult) payload.body;
			return Optional.ofNullable(resolver.getSiteId(body.allocationIdentifier));
		}
		if(payload.body.getClass().equals(AgentProjectAllocationUpdate.class)) {
			AgentProjectAllocationUpdate body = (AgentProjectAllocationUpdate) payload.body;
			return Optional.ofNullable(resolver.getSiteId(body.allocationIdentifier));
		}
		throw new IllegalStateException("Error - not applicable class was send to process");
	}
}
