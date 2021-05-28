/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.site.api.message_resolver.UserAllocationGrantMessageResolver;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class SSHKeyMessageResolverImpl implements UserAllocationGrantMessageResolver {
	private final SSHKeyOperationRepository sshKeyOperationRepository;
	private final SiteRepository siteRepository;

	SSHKeyMessageResolverImpl(SSHKeyOperationRepository sshKeyOperationRepository, SiteRepository siteRepository) {
		this.sshKeyOperationRepository = sshKeyOperationRepository;
		this.siteRepository = siteRepository;
	}

	@Override
	@Transactional
	public boolean isMessageCorrelated(CorrelationId id, SiteExternalId siteExternalId) {
		SSHKeyOperationJob job = sshKeyOperationRepository.findByCorrelationId(id);
		SiteExternalId externalId = siteRepository.findByIdExternalId(job.siteId);
		return externalId.equals(siteExternalId);
	}
}
