/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.site_agent_pending_message;

import io.imunity.furms.api.site_agent_pending_message.SiteAgentConnectionService;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.site.api.message_remover.SiteAgentPendingMessageRemoverConnector;
import io.imunity.furms.site.api.site_agent.SiteAgentRetryService;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import io.imunity.furms.spi.site_agent_pending_message.SiteAgentPendingMessageRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteAgentConnectionServiceImplTest {

	@Mock
	private SiteAgentPendingMessageRepository repository;
	@Mock
	private SiteAgentRetryService siteAgentRetryService;
	@Mock
	private SiteAgentStatusService siteAgentStatusService;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private SiteAgentPendingMessageRemoverConnector siteAgentPendingMessageRemoverConnector;

	private final Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);

	private SiteAgentConnectionService service;

	@BeforeEach
	void setUp() {
		service = new SiteAgentConnectionServiceImpl(repository, siteAgentRetryService, siteAgentStatusService, siteRepository, clock, siteAgentPendingMessageRemoverConnector);
	}

	@Test
	void findAll() {
		SiteId id = new SiteId(UUID.randomUUID());

		service.findAll(id);

		verify(repository).findAll(id);
	}

	@Test
	void shouldRetry() {
		SiteId id = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("externalId"));
		CorrelationId correlationId = CorrelationId.randomID();
		String json = "json";
		SiteAgentPendingMessage message = SiteAgentPendingMessage.builder()
			.siteExternalId(id.externalId)
			.jsonContent(json)
			.build();

		when(repository.find(correlationId)).thenReturn(Optional.of(message));
		when(siteRepository.findById(id)).thenReturn(Optional.of(
			Site.builder()
				.id(id)
				.build()
		));

		service.retry(id, correlationId);

		verify(siteAgentRetryService).retry(id.externalId, json);
		verify(repository).overwriteSentTime(correlationId, ZonedDateTime.now(clock).toLocalDateTime());
	}

	@Test
	void shouldNotRetryWhenSiteIdAndSiteExternalIdAreNotRelated() {
		SiteId id = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("externalId"));
		CorrelationId correlationId = CorrelationId.randomID();
		String json = "json";
		SiteAgentPendingMessage message = SiteAgentPendingMessage.builder()
			.siteExternalId(new SiteExternalId("externalId1"))
			.jsonContent(json)
			.build();

		when(repository.find(correlationId)).thenReturn(Optional.of(message));
		when(siteRepository.findById(id)).thenReturn(Optional.of(
			Site.builder()
				.id(id)
				.build()
		));

		assertThrows(IllegalArgumentException.class, () -> service.retry(id, correlationId));

		verify(siteAgentRetryService, times(0)).retry(id.externalId, json);
		verify(repository, times(0)).overwriteSentTime(correlationId, ZonedDateTime.now(clock).toLocalDateTime());
	}

	@Test
	void delete() {
		SiteId id = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("externalId"));
		CorrelationId correlationId = CorrelationId.randomID();
		String json = "json";
		SiteAgentPendingMessage message = SiteAgentPendingMessage.builder()
			.siteExternalId(id.externalId)
			.jsonContent(json)
			.build();

		when(repository.find(correlationId)).thenReturn(Optional.of(message));
		when(siteRepository.findById(id)).thenReturn(Optional.of(
			Site.builder()
				.id(id)
				.build()
		));

		service.delete(id, correlationId);

		verify(siteAgentPendingMessageRemoverConnector).remove(correlationId, json);
		verify(repository).delete(correlationId);
	}

	@Test
	void shouldNotDeleteWhenSiteIdAndSiteExternalIdAreNotRelated() {
		SiteId id = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("externalId"));
		CorrelationId correlationId = CorrelationId.randomID();
		String json = "json";
		SiteAgentPendingMessage message = SiteAgentPendingMessage.builder()
			.siteExternalId(new SiteExternalId("externalId1"))
			.jsonContent(json)
			.build();

		when(repository.find(correlationId)).thenReturn(Optional.of(message));
		when(siteRepository.findById(id)).thenReturn(Optional.of(
			Site.builder()
				.id(id)
				.build()
		));

		assertThrows(IllegalArgumentException.class, () -> service.delete(id, correlationId));

		verify(siteAgentPendingMessageRemoverConnector, times(0)).remove(correlationId, json);
		verify(repository, times(0)).delete(correlationId);
	}

	@Test
	void shouldGetSiteAgentStatus() {
		SiteId id = new SiteId(UUID.randomUUID());
		SiteExternalId externalId = new SiteExternalId("externalId");

		when(siteRepository.findByIdExternalId(id)).thenReturn(externalId);

		service.getSiteAgentStatus(id);

		verify(siteAgentStatusService).getStatus(externalId);
	}
}