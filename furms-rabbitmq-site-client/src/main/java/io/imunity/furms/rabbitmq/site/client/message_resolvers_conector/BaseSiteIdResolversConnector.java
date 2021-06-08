/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_resolvers_conector;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.message_resolver.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
class BaseSiteIdResolversConnector implements SiteIdResolversConnector {
	private final Map<Class<? extends Body>, BaseSiteIdResolver> resolvers;

	BaseSiteIdResolversConnector(
		ProjectAllocationInstallationSiteIdResolver projectAllocationInstallationSiteIdResolver,
		ProjectInstallationSiteIdResolver projectInstallationSiteIdResolver,
		ProjectUpdateSiteIdResolver projectUpdateSiteIdResolver,
		SSHKeySiteIdResolver sshKeySiteIdResolver,
		UserAdditionSiteIdResolver userAdditionSiteIdResolver,
		UserAllocationGrantSiteIdResolver userAllocationGrantSiteIdResolver) {
		this.resolvers = Map.ofEntries(
			Map.entry(AgentProjectAllocationInstallationAck.class, projectAllocationInstallationSiteIdResolver),

			Map.entry(AgentProjectInstallationRequestAck.class, projectInstallationSiteIdResolver),
			Map.entry(AgentProjectInstallationResult.class, projectInstallationSiteIdResolver),

			Map.entry(AgentProjectUpdateRequestAck.class, projectUpdateSiteIdResolver),
			Map.entry(AgentProjectUpdateRequest.class, projectUpdateSiteIdResolver),

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
		return resolvers.get(payload.body.getClass()).getSiteId(new CorrelationId(messageCorrelationId));
	}
}
