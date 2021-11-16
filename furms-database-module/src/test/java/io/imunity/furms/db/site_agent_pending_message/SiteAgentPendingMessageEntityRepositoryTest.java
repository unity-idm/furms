/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.site_agent_pending_message;


import io.imunity.furms.db.DBIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SiteAgentPendingMessageEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteAgentPendingMessageEntityRepository repository;


	@Test
	void shouldFindByCorrelationId(){
		SiteAgentPendingMessageEntity entity = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		SiteAgentPendingMessageEntity saved = repository.save(entity);

		SiteAgentPendingMessageEntity entity1 = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		repository.save(entity1);

		SiteAgentPendingMessageEntity entity2 = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		repository.save(entity2);

		Optional<SiteAgentPendingMessageEntity> found = repository.findByCorrelationId(saved.correlationId);
		assertTrue(found.isPresent());
		assertEquals(saved, found.get());
	}

	@Test
	void shouldFindAllBySiteExternalId(){
		SiteAgentPendingMessageEntity entity = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		SiteAgentPendingMessageEntity saved = repository.save(entity);

		SiteAgentPendingMessageEntity entity1 = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		SiteAgentPendingMessageEntity saved1 = repository.save(entity1);

		SiteAgentPendingMessageEntity entity2 = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId1")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		repository.save(entity2);

		Set<SiteAgentPendingMessageEntity> found = repository.findAllBySiteExternalId("externalId");
		assertEquals(2, found.size());
		assertEquals(Set.of(saved, saved1), found);
	}

	@Test
	void shouldCreate(){
		SiteAgentPendingMessageEntity entity = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		SiteAgentPendingMessageEntity saved = repository.save(entity);
		Optional<SiteAgentPendingMessageEntity> found = repository.findById(saved.getId());
		assertTrue(found.isPresent());
		assertEquals(saved, found.get());
	}

	@Test
	void shouldUpdate(){
		SiteAgentPendingMessageEntity entity = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		SiteAgentPendingMessageEntity saved = repository.save(entity);

		SiteAgentPendingMessageEntity updateEntity = SiteAgentPendingMessageEntity.builder()
			.id(saved.getId())
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId1")
			.sentAt(LocalDate.now().atStartOfDay())
			.ackAt(LocalDate.now().atStartOfDay())
			.retryAmount(2)
			.jsonContent("json1")
			.build();
		SiteAgentPendingMessageEntity updated = repository.save(updateEntity);

		Optional<SiteAgentPendingMessageEntity> found = repository.findById(saved.getId());
		assertTrue(found.isPresent());
		assertEquals(updated, found.get());
	}

	@Test
	void shouldDeleteByCorrelationId(){
		SiteAgentPendingMessageEntity entity = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		SiteAgentPendingMessageEntity saved = repository.save(entity);

		repository.deleteByCorrelationId(saved.correlationId);

		Optional<SiteAgentPendingMessageEntity> found = repository.findById(saved.getId());
		assertTrue(found.isEmpty());
	}

	@Test
	void shouldDeleteById(){
		SiteAgentPendingMessageEntity entity = SiteAgentPendingMessageEntity.builder()
			.correlationId(UUID.randomUUID())
			.siteExternalId("externalId")
			.sentAt(LocalDate.now().atStartOfDay())
			.jsonContent("json")
			.build();
		SiteAgentPendingMessageEntity saved = repository.save(entity);

		repository.deleteById(saved.getId());

		Optional<SiteAgentPendingMessageEntity> found = repository.findById(saved.getId());
		assertTrue(found.isEmpty());
	}
}