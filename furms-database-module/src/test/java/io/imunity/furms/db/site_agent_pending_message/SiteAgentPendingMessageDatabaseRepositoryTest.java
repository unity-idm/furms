/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.site_agent_pending_message;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.site_agent_pending_message.SiteAgentPendingMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class SiteAgentPendingMessageDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteAgentPendingMessageEntityRepository entityRepository;

	@Autowired
	private SiteAgentPendingMessageRepository repository;

	@Test
	void shouldFindAll(){
		SiteAgentPendingMessageEntity entity = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		entityRepository.save(entity);

		SiteAgentPendingMessageEntity entity1 = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json1")
			.build();
		entityRepository.save(entity1);

		SiteAgentPendingMessageEntity entity2 = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId1")
			.sentAt(LocalDate.now().atStartOfDay())
			.ackAt(LocalDate.now().atStartOfDay())
			.jsonContent("json2")
			.build();
		entityRepository.save(entity2);

		Set<SiteAgentPendingMessage> all = repository.findAll(new SiteExternalId("externalId"));

		assertThat(all.size()).isEqualTo(2);
		assertThat(all.stream().map(m -> m.correlationId.id).collect(toSet()))
			.isEqualTo(Set.of(entity.correlationId.toString(), entity1.correlationId.toString()));
		assertThat(all.stream().map(m -> m.siteExternalId.id).collect(toSet())).isEqualTo(Set.of("externalId"));
		assertThat(all.stream().map(m -> m.jsonContent).collect(toSet())).isEqualTo(Set.of("json", "json1"));
	}

	@Test
	void shouldFind(){
		LocalDateTime sentAt = LocalDate.now().atStartOfDay();
		SiteAgentPendingMessageEntity entity = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(sentAt)
			.jsonContent("json")
			.build();
		entityRepository.save(entity);

		CorrelationId correlationId = new CorrelationId(entity.correlationId.toString());
		Optional<SiteAgentPendingMessage> siteAgentPendingMessage = repository.find(correlationId);

		assertThat(siteAgentPendingMessage).isPresent();
		assertThat(siteAgentPendingMessage.get().correlationId).isEqualTo(correlationId);
		assertThat(siteAgentPendingMessage.get().jsonContent).isEqualTo("json");
		assertThat(siteAgentPendingMessage.get().siteExternalId.id).isEqualTo("externalId");
		assertThat(siteAgentPendingMessage.get().utcSentAt).isEqualTo(sentAt);
	}

	@Test
	void shouldCreate(){
		LocalDateTime sentAt = LocalDate.now().atStartOfDay();
		SiteAgentPendingMessage message = SiteAgentPendingMessage.builder()
			.correlationId(CorrelationId.randomID())
			.siteExternalId(new SiteExternalId("externalId"))
			.utcSentAt(sentAt)
			.jsonContent("json")
			.build();
		repository.create(message);

		Optional<SiteAgentPendingMessageEntity> siteAgentPendingMessageEntity = entityRepository.findByCorrelationId(UUID.fromString(message.correlationId.id));

		assertThat(siteAgentPendingMessageEntity).isPresent();
		assertThat(siteAgentPendingMessageEntity.get().correlationId).isEqualTo(UUID.fromString(message.correlationId.id));
		assertThat(siteAgentPendingMessageEntity.get().jsonContent).isEqualTo("json");
		assertThat(siteAgentPendingMessageEntity.get().siteExternalId).isEqualTo("externalId");
		assertThat(siteAgentPendingMessageEntity.get().sentAt).isEqualTo(sentAt);
	}

	@Test
	void shouldOverwriteSentTime(){
		LocalDateTime sentAt = LocalDate.now().atStartOfDay().minusDays(2);
		LocalDateTime newSentAt = LocalDate.now().atStartOfDay();
		SiteAgentPendingMessage message = SiteAgentPendingMessage.builder()
			.correlationId(CorrelationId.randomID())
			.siteExternalId(new SiteExternalId("externalId"))
			.utcSentAt(sentAt)
			.jsonContent("json")
			.build();
		repository.create(message);

		repository.overwriteSentTime(message.correlationId, newSentAt);

		Optional<SiteAgentPendingMessageEntity> siteAgentPendingMessageEntity = entityRepository.findByCorrelationId(UUID.fromString(message.correlationId.id));

		assertThat(siteAgentPendingMessageEntity).isPresent();
		assertThat(siteAgentPendingMessageEntity.get().correlationId).isEqualTo(UUID.fromString(message.correlationId.id));
		assertThat(siteAgentPendingMessageEntity.get().jsonContent).isEqualTo("json");
		assertThat(siteAgentPendingMessageEntity.get().siteExternalId).isEqualTo("externalId");
		assertThat(siteAgentPendingMessageEntity.get().sentAt).isEqualTo(newSentAt);
		assertThat(siteAgentPendingMessageEntity.get().retryAmount).isEqualTo(1);
	}

	@Test
	void shouldUpdateAckTime(){
		LocalDateTime sentAt = LocalDate.now().atStartOfDay().minusDays(2);
		LocalDateTime ackAt = LocalDate.now().atStartOfDay();
		SiteAgentPendingMessage message = SiteAgentPendingMessage.builder()
			.correlationId(CorrelationId.randomID())
			.siteExternalId(new SiteExternalId("externalId"))
			.utcSentAt(sentAt)
			.jsonContent("json")
			.build();
		repository.create(message);

		repository.updateAckTime(message.correlationId, ackAt);

		Optional<SiteAgentPendingMessageEntity> siteAgentPendingMessageEntity = entityRepository.findByCorrelationId(UUID.fromString(message.correlationId.id));

		assertThat(siteAgentPendingMessageEntity).isPresent();
		assertThat(siteAgentPendingMessageEntity.get().correlationId).isEqualTo(UUID.fromString(message.correlationId.id));
		assertThat(siteAgentPendingMessageEntity.get().jsonContent).isEqualTo("json");
		assertThat(siteAgentPendingMessageEntity.get().siteExternalId).isEqualTo("externalId");
		assertThat(siteAgentPendingMessageEntity.get().sentAt).isEqualTo(sentAt);
		assertThat(siteAgentPendingMessageEntity.get().ackAt).isEqualTo(ackAt);
		assertThat(siteAgentPendingMessageEntity.get().retryAmount).isEqualTo(0);
	}

	@Test
	void shouldDelete(){
		LocalDateTime sentAt = LocalDate.now().atStartOfDay();
		SiteAgentPendingMessage message = SiteAgentPendingMessage.builder()
			.correlationId(CorrelationId.randomID())
			.siteExternalId(new SiteExternalId("externalId"))
			.utcSentAt(sentAt)
			.jsonContent("json")
			.build();
		repository.create(message);

		repository.delete(message.correlationId);

		Optional<SiteAgentPendingMessageEntity> siteAgentPendingMessageEntity = entityRepository.findByCorrelationId(UUID.fromString(message.correlationId.id));

		assertThat(siteAgentPendingMessageEntity).isEmpty();
	}

}