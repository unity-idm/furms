/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.site_agent_pending_message;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.site_agent_pending_message.SiteAgentPendingMessageRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class SiteAgentPendingMessageDatabaseRepository implements SiteAgentPendingMessageRepository {
	private final SiteRepository siteRepository;
	private final SiteAgentPendingMessageEntityRepository repository;

	SiteAgentPendingMessageDatabaseRepository(SiteRepository siteRepository, SiteAgentPendingMessageEntityRepository repository) {
		this.siteRepository = siteRepository;
		this.repository = repository;
	}

	@Override
	public Set<SiteAgentPendingMessage> findAll(SiteId siteId) {
		return repository.findAllBySiteId(siteId.id).stream()
			.map(message ->
				SiteAgentPendingMessage.builder()
					.siteExternalId(new SiteExternalId(message.siteExternalId))
					.correlationId(new CorrelationId(message.correlationId.toString()))
					.jsonContent(message.jsonContent)
					.retryCount(message.retryCount)
					.utcSentAt(message.sentAt)
					.utcAckAt(message.ackAt)
					.build()
			)
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<SiteAgentPendingMessage> find(CorrelationId correlationId) {
		return repository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(message ->
				SiteAgentPendingMessage.builder()
					.siteExternalId(new SiteExternalId(message.siteExternalId))
					.correlationId(new CorrelationId(message.correlationId.toString()))
					.jsonContent(message.jsonContent)
					.retryCount(message.retryCount)
					.utcSentAt(message.sentAt)
					.utcAckAt(message.ackAt)
					.build()
			);
	}

	@Override
	public void create(SiteAgentPendingMessage message) {
		repository.save(SiteAgentPendingMessageEntity.builder()
			.siteId(siteRepository.findByExternalId(message.siteExternalId).id)
			.siteExternalId(message.siteExternalId.id)
			.correlationId(UUID.fromString(message.correlationId.id))
			.jsonContent(message.jsonContent)
			.retryCount(message.retryCount)
			.sentAt(message.utcSentAt)
			.build());
	}

	@Override
	public void updateAckTime(CorrelationId id, LocalDateTime ackAt) {
		repository.findByCorrelationId(UUID.fromString(id.id))
			.map(message ->
				SiteAgentPendingMessageEntity.builder()
					.id(message.getId())
					.siteId(message.siteId)
					.siteExternalId(message.siteExternalId)
					.correlationId(message.correlationId)
					.jsonContent(message.jsonContent)
					.retryCount(message.retryCount)
					.sentAt(message.sentAt)
					.ackAt(ackAt)
					.build()
			)
			.ifPresent(repository::save);
	}

	@Override
	public void overwriteSentTime(CorrelationId id, LocalDateTime sentAt) {
		repository.findByCorrelationId(UUID.fromString(id.id))
			.map(message ->
				SiteAgentPendingMessageEntity.builder()
					.id(message.getId())
					.siteId(message.siteId)
					.siteExternalId(message.siteExternalId)
					.correlationId(message.correlationId)
					.jsonContent(message.jsonContent)
					.retryCount(message.retryCount + 1)
					.sentAt(sentAt)
					.build()
			)
			.ifPresent(repository::save);
	}

	@Override
	public void delete(CorrelationId id) {
		repository.deleteByCorrelationId(UUID.fromString(id.id));
	}
}
