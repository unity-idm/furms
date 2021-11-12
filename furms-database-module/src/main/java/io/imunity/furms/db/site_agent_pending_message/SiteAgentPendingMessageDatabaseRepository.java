/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.site_agent_pending_message;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.site_agent_pending_message.SiteAgentPendingMessageRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class SiteAgentPendingMessageDatabaseRepository implements SiteAgentPendingMessageRepository {
	private final SiteAgentPendingMessageEntityRepository repository;

	SiteAgentPendingMessageDatabaseRepository(SiteAgentPendingMessageEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Set<SiteAgentPendingMessage> findAll(SiteExternalId siteId) {
		return repository.findAllBySiteExternalId(siteId.id).stream()
			.map(message ->
				SiteAgentPendingMessage.builder()
					.siteExternalId(new SiteExternalId(message.siteExternalId))
					.correlationId(new CorrelationId(message.correlationId.toString()))
					.jsonContent(message.jsonContent)
					.retryAmount(message.retryAmount)
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
					.retryAmount(message.retryAmount)
					.utcSentAt(message.sentAt)
					.utcAckAt(message.ackAt)
					.build()
			);
	}

	@Override
	public void create(SiteAgentPendingMessage message) {
		repository.save(SiteAgentPendingMessageEntity.builder()
			.siteExternalId(message.siteExternalId.id)
			.correlationId(UUID.fromString(message.correlationId.id))
			.jsonContent(message.jsonContent)
			.retryAmount(message.retryAmount)
			.sentAt(message.utcSentAt)
			.build());
	}

	@Override
	public void updateAckTime(CorrelationId id, LocalDateTime ackAt) {
		repository.findByCorrelationId(UUID.fromString(id.id))
			.map(message ->
				SiteAgentPendingMessageEntity.builder()
					.siteExternalId(message.siteExternalId)
					.correlationId(message.correlationId)
					.jsonContent(message.jsonContent)
					.retryAmount(message.retryAmount)
					.sentAt(message.sentAt)
					.ackAt(ackAt)
					.build()
			)
			.ifPresent(repository::save);
	}

	@Override
	public void restartSentTime(CorrelationId id, LocalDateTime sentAt) {
		repository.findByCorrelationId(UUID.fromString(id.id))
			.map(message ->
				SiteAgentPendingMessageEntity.builder()
					.siteExternalId(message.siteExternalId)
					.correlationId(message.correlationId)
					.jsonContent(message.jsonContent)
					.retryAmount(message.retryAmount + 1)
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
