/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_resolvers_conector;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectDeallocationRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationResult;
import io.imunity.furms.rabbitmq.site.models.AgentProjectUpdateRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectUpdateResult;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionAck;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionResult;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalAck;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalResult;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingAck;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingResult;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusRequestAck;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusResult;
import io.imunity.furms.rabbitmq.site.models.UserAllocationBlockAccessRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserAllocationBlockAccessResult;
import io.imunity.furms.rabbitmq.site.models.UserAllocationGrantAccessRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserAllocationGrantAccessResult;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddResult;
import io.imunity.furms.rabbitmq.site.models.UserProjectRemovalRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserProjectRemovalResult;
import io.imunity.furms.site.api.SiteAgentPendingMessageResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class BaseSiteIdResolversConnector implements SiteIdResolversConnector {
	private final Set<Class<? extends Body>> applicable = Set.of(
		AgentProjectAllocationInstallationAck.class, AgentProjectDeallocationRequestAck.class,
		AgentProjectInstallationRequestAck.class, AgentProjectInstallationResult.class,
		AgentProjectUpdateRequestAck.class, AgentProjectUpdateResult.class,
		AgentSSHKeyAdditionAck.class, AgentSSHKeyAdditionResult.class,
		AgentSSHKeyRemovalAck.class, AgentSSHKeyRemovalResult.class,
		AgentSSHKeyUpdatingAck.class, AgentSSHKeyUpdatingResult.class,
		UserProjectAddRequestAck.class, UserProjectAddResult.class,
		UserProjectRemovalRequestAck.class, UserProjectRemovalResult.class,
		UserAllocationGrantAccessRequestAck.class, UserAllocationGrantAccessResult.class,
		UserAllocationBlockAccessRequestAck.class, UserAllocationBlockAccessResult.class,
		SetUserStatusRequestAck.class, SetUserStatusResult.class
	);
	private final SiteAgentPendingMessageResolver resolver;

	BaseSiteIdResolversConnector(SiteAgentPendingMessageResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Set<Class<? extends Body>> getApplicableClasses() {
		return applicable;
	}

	@Override
	public SiteExternalId getSiteId(Payload<?> payload) {
		String messageCorrelationId = payload.header.messageCorrelationId;
		return resolver.find(new CorrelationId(messageCorrelationId))
			.map(message -> message.siteExternalId)
			.orElse(null);
	}
}
