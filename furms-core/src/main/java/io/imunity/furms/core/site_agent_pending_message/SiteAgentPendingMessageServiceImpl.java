/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.site_agent_pending_message;

import io.imunity.furms.api.site_agent_pending_message.SiteAgentPendingMessageService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.site.api.site_agent.SiteAgentRetryService;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import io.imunity.furms.spi.site_agent_pending_message.SiteAgentPendingMessageRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.utils.UTCTimeUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
class SiteAgentPendingMessageServiceImpl implements SiteAgentPendingMessageService {
	private final SiteAgentPendingMessageRepository repository;
	private final SiteAgentRetryService siteAgentRetryService;
	private final SiteAgentStatusService siteAgentStatusService;
	private final SiteRepository siteRepository;
	private final Clock clock;

	SiteAgentPendingMessageServiceImpl(SiteAgentPendingMessageRepository repository,
	                                   SiteAgentRetryService siteAgentRetryService,
	                                   SiteAgentStatusService siteAgentStatusService,
	                                   SiteRepository siteRepository,
	                                   Clock clock) {
		this.repository = repository;
		this.siteAgentRetryService = siteAgentRetryService;
		this.siteAgentStatusService = siteAgentStatusService;
		this.siteRepository = siteRepository;
		this.clock = clock;
	}

	@Override
	public Set<SiteAgentPendingMessage> findAll(SiteId siteId) {
		return siteRepository.findById(siteId.id).stream()
			.flatMap(site -> repository.findAll(site.getExternalId()).stream())
			.collect(Collectors.toSet());
	}

	@Override
	public void retry(CorrelationId correlationId) {
		repository.find(correlationId).ifPresent(message -> {
			siteAgentRetryService.retry(new SiteExternalId(message.siteExternalId.id), message.jsonContent);
			repository.restartSentTime(correlationId, UTCTimeUtils.convertToUTCTime(ZonedDateTime.now(clock)));
		});
	}

	@Override
	public void delete(CorrelationId correlationId) {
		repository.delete(correlationId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId")
	public PendingJob<SiteAgentStatus> getSiteAgentStatus(String siteId) {
		SiteExternalId externalId = siteRepository.findByIdExternalId(siteId);
		return siteAgentStatusService.getStatus(externalId);
	}
}
