/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.site_agent_pending_message;

import io.imunity.furms.api.site_agent_pending_message.SiteAgentConnectionService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.site.api.message_remover.SiteAgentPendingMessageRemoverConnector;
import io.imunity.furms.site.api.site_agent.SiteAgentRetryService;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import io.imunity.furms.spi.site_agent_pending_message.SiteAgentPendingMessageRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.utils.UTCTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
class SiteAgentConnectionServiceImpl implements SiteAgentConnectionService {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteAgentPendingMessageRepository repository;
	private final SiteAgentRetryService siteAgentRetryService;
	private final SiteAgentStatusService siteAgentStatusService;
	private final SiteRepository siteRepository;
	private final Clock clock;
	private final SiteAgentPendingMessageRemoverConnector siteAgentPendingMessageRemoverConnector;

	SiteAgentConnectionServiceImpl(SiteAgentPendingMessageRepository repository,
	                               SiteAgentRetryService siteAgentRetryService,
	                               SiteAgentStatusService siteAgentStatusService,
	                               SiteRepository siteRepository,
	                               Clock clock,
	                               SiteAgentPendingMessageRemoverConnector siteAgentPendingMessageRemoverConnector) {
		this.repository = repository;
		this.siteAgentRetryService = siteAgentRetryService;
		this.siteAgentStatusService = siteAgentStatusService;
		this.siteRepository = siteRepository;
		this.clock = clock;
		this.siteAgentPendingMessageRemoverConnector = siteAgentPendingMessageRemoverConnector;
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId.id")
	public Set<SiteAgentPendingMessage> findAll(SiteId siteId) {
		return repository.findAll(siteId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId.id")
	public void retry(SiteId siteId, CorrelationId correlationId) {
		repository.find(correlationId).ifPresentOrElse(
			message -> {
				if(isMessageRelatedToSite(siteId, message)) {
					siteAgentRetryService.retry(new SiteExternalId(message.siteExternalId.id), message.jsonContent);
					repository.overwriteSentTime(correlationId, UTCTimeUtils.convertToUTCTime(ZonedDateTime.now(clock)));
					return;
				}
				LOG.warn("Correlation id {} doesn't belong to site id {}", correlationId, siteId);
			},
			() -> LOG.warn("Correlation id {} doesn't exist", correlationId));
	}

	private Boolean isMessageRelatedToSite(SiteId siteId, SiteAgentPendingMessage message) {
		return siteRepository.findById(siteId.id).map(site -> site.getExternalId().equals(message.siteExternalId))
			.orElse(false);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId.id")
	public void delete(SiteId siteId, CorrelationId correlationId) {
		repository.find(correlationId).ifPresentOrElse(
			message -> {
				if(isMessageRelatedToSite(siteId, message)) {
					siteAgentPendingMessageRemoverConnector.remove(correlationId, message.jsonContent);
					repository.delete(correlationId);
					return;
				}
				LOG.warn("Correlation id {} doesn't belong to site id {}", correlationId, siteId);
			},
			() -> LOG.warn("Correlation id {} doesn't exist", correlationId));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId.id")
	public PendingJob<SiteAgentStatus> getSiteAgentStatus(SiteId siteId) {
		SiteExternalId externalId = siteRepository.findByIdExternalId(siteId.id);
		return siteAgentStatusService.getStatus(externalId);
	}
}
