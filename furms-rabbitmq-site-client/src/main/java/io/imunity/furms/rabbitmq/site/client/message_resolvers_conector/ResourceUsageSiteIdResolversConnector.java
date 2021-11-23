/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_resolvers_conector;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.CumulativeResourceUsageRecord;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.UserResourceUsageRecord;
import io.imunity.furms.site.api.message_resolver.ResourceUsageSiteIdResolver;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
class ResourceUsageSiteIdResolversConnector implements SiteIdResolversConnector {
	private final Set<Class<? extends Body>> applicable = Set.of(CumulativeResourceUsageRecord.class, UserResourceUsageRecord.class);
	private final ResourceUsageSiteIdResolver resolver;

	ResourceUsageSiteIdResolversConnector(ResourceUsageSiteIdResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Set<Class<? extends Body>> getApplicableClasses() {
		return applicable;
	}

	@Override
	public Optional<SiteExternalId> getSiteId(Payload<?> payload) {
		if(payload.body.getClass().equals(CumulativeResourceUsageRecord.class)){
			CumulativeResourceUsageRecord body = (CumulativeResourceUsageRecord)payload.body;
			return Optional.ofNullable(resolver.getSiteId(body.projectIdentifier, body.allocationIdentifier));
		}
		if(payload.body.getClass().equals(UserResourceUsageRecord.class)){
			UserResourceUsageRecord body = (UserResourceUsageRecord)payload.body;
			return Optional.ofNullable(resolver.getSiteId(body.projectIdentifier, body.allocationIdentifier));
		}
		throw new IllegalStateException("Error - not applicable class was send to process");
	}
}
