/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_resolvers_conector;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.client.SiteAgentStatusServiceImpl;
import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
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
import io.imunity.furms.rabbitmq.site.models.UserAllocationBlockAccessRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserAllocationBlockAccessResult;
import io.imunity.furms.rabbitmq.site.models.UserAllocationGrantAccessRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserAllocationGrantAccessResult;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddResult;
import io.imunity.furms.rabbitmq.site.models.UserProjectRemovalRequestAck;
import io.imunity.furms.rabbitmq.site.models.UserProjectRemovalResult;
import io.imunity.furms.site.api.message_resolver.BaseSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ProjectDeallocationSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.ProjectUpdateSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.SSHKeySiteIdResolver;
import io.imunity.furms.site.api.message_resolver.UserAdditionSiteIdResolver;
import io.imunity.furms.site.api.message_resolver.UserAllocationGrantSiteIdResolver;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
class BaseSiteIdResolversConnector implements SiteIdResolversConnector {
	private final Map<Class<? extends Body>, BaseSiteIdResolver> resolvers;

	BaseSiteIdResolversConnector(
		SiteAgentStatusServiceImpl siteAgentStatusService,
		ProjectDeallocationSiteIdResolver projectDeallocationSiteIdResolver,
		ProjectAllocationInstallationSiteIdResolver projectAllocationInstallationSiteIdResolver,
		ProjectInstallationSiteIdResolver projectInstallationSiteIdResolver,
		ProjectUpdateSiteIdResolver projectUpdateSiteIdResolver,
		SSHKeySiteIdResolver sshKeySiteIdResolver,
		UserAdditionSiteIdResolver userAdditionSiteIdResolver,
		UserAllocationGrantSiteIdResolver userAllocationGrantSiteIdResolver) {
		this.resolvers = Map.ofEntries(
			Map.entry(AgentPingAck.class, siteAgentStatusService),

			Map.entry(AgentProjectAllocationInstallationAck.class, projectAllocationInstallationSiteIdResolver),
			Map.entry(AgentProjectDeallocationRequestAck.class, projectDeallocationSiteIdResolver),

			Map.entry(AgentProjectInstallationRequestAck.class, projectInstallationSiteIdResolver),
			Map.entry(AgentProjectInstallationResult.class, projectInstallationSiteIdResolver),

			Map.entry(AgentProjectUpdateRequestAck.class, projectUpdateSiteIdResolver),
			Map.entry(AgentProjectUpdateResult.class, projectUpdateSiteIdResolver),

			Map.entry(AgentSSHKeyAdditionAck.class, sshKeySiteIdResolver),
			Map.entry(AgentSSHKeyAdditionResult.class, sshKeySiteIdResolver),
			Map.entry(AgentSSHKeyRemovalAck.class, sshKeySiteIdResolver),
			Map.entry(AgentSSHKeyRemovalResult.class, sshKeySiteIdResolver),
			Map.entry(AgentSSHKeyUpdatingAck.class, sshKeySiteIdResolver),
			Map.entry(AgentSSHKeyUpdatingResult.class, sshKeySiteIdResolver),

			Map.entry(UserProjectAddRequestAck.class, userAdditionSiteIdResolver),
			Map.entry(UserProjectAddResult.class, userAdditionSiteIdResolver),
			Map.entry(UserProjectRemovalRequestAck.class, userAdditionSiteIdResolver),
			Map.entry(UserProjectRemovalResult.class, userAdditionSiteIdResolver),

			Map.entry(UserAllocationGrantAccessRequestAck.class, userAllocationGrantSiteIdResolver),
			Map.entry(UserAllocationGrantAccessResult.class, userAllocationGrantSiteIdResolver),
			Map.entry(UserAllocationBlockAccessRequestAck.class, userAllocationGrantSiteIdResolver),
			Map.entry(UserAllocationBlockAccessResult.class, userAllocationGrantSiteIdResolver)
		);
	}

	@Override
	public Set<Class<? extends Body>> getApplicableClasses() {
		return resolvers.keySet();
	}

	@Override
	public SiteExternalId getSiteId(Payload<?> payload) {
		String messageCorrelationId = payload.header.messageCorrelationId;
		if(messageCorrelationId == null)
			return null;
		return resolvers.get(payload.body.getClass()).getSiteId(new CorrelationId(messageCorrelationId));
	}
}
