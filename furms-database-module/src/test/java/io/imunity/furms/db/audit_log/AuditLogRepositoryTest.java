/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.audit_log;


import io.imunity.furms.db.DBIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuditLogRepositoryTest extends DBIntegrationTest {

	@Autowired
	private AuditLogEntityRepository auditLogRepository;

	@Test
	void shouldCreate() {
		LocalDateTime now = LocalDate.now().atStartOfDay();
		AuditLogEntity auditLog = AuditLogEntity.builder()
			.originatorId("originatorId")
			.operationCategory(1)
			.operationAction(1)
			.operationSubject("name")
			.dataJson("dataJson")
			.creationTime(now)
			.build();

		AuditLogEntity saved = auditLogRepository.save(auditLog);
		AuditLogEntity found = auditLogRepository.findById(saved.getId()).get();

		assertEquals(saved, found);
	}

	@Test
	@Disabled
	void shouldHaveGoodPerformance() {
		LocalDateTime now = LocalDate.now().atStartOfDay();
		Random rand = new Random();

		Set<AuditLogEntity> auditLogEntities = new HashSet<>();
		for(int i =0; i < 1000; i++)
			auditLogEntities.add(AuditLogEntity.builder()
				.originatorId("originatorId" + i)
				.operationCategory(rand.nextInt(16))
				.operationAction(rand.nextInt(8))
				.operationSubject("name" + i)
				.dataJson("dataJson" + i)
				.creationTime(now.minusMinutes(i))
				.build());
		for(int i = 0; i < 5000; i++) {
			auditLogRepository.saveAll(auditLogEntities);
		}

		long start = System.currentTimeMillis();
		Set<AuditLogEntity> found = auditLogRepository.findByCreationTimeBetweenAndOperationActionInAndOperationCategoryInAndOperationSubjectContainingAndOriginatorIdInOrOriginatorPersistenceIdIn(
			now.minusDays(3), now.plusDays(3), Set.of(1, 3, 4), Set.of(1, 2, 5), "", Set.of("originatorId", "originatorId1"), Set.of()
		);
		long end = System.currentTimeMillis();

		assertTrue(TimeUnit.MILLISECONDS.toSeconds(end - start) < 1);
	}

	@Test
	void shouldFound() {
		LocalDateTime now = LocalDate.now().atStartOfDay();
		AuditLogEntity auditLog = AuditLogEntity.builder()
			.originatorId("originatorId")
			.operationCategory(1)
			.operationAction(1)
			.operationSubject("name")
			.dataJson("dataJson")
			.creationTime(now)
			.build();

		AuditLogEntity auditLog1 = AuditLogEntity.builder()
			.originatorId("originatorId1")
			.operationCategory(2)
			.operationAction(3)
			.operationSubject("name")
			.dataJson("dataJson")
			.creationTime(now.plusDays(1))
			.build();

		AuditLogEntity auditLog2 = AuditLogEntity.builder()
			.originatorId("originatorId")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name")
			.dataJson("dataJson")
			.creationTime(now.minusDays(1))
			.build();

		AuditLogEntity auditLogWrongDate = AuditLogEntity.builder()
			.originatorId("originatorId1")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name")
			.dataJson("dataJson")
			.creationTime(now.minusDays(10))
			.build();

		AuditLogEntity auditLogWrongDate1 = AuditLogEntity.builder()
			.originatorId("originatorId")
			.operationCategory(2)
			.operationAction(3)
			.operationSubject("name")
			.dataJson("dataJson")
			.creationTime(now.plusDays(10))
			.build();

		AuditLogEntity auditLogWrongAction = AuditLogEntity.builder()
			.originatorId("originatorId1")
			.operationCategory(5)
			.operationAction(7)
			.operationSubject("name")
			.dataJson("dataJson")
			.creationTime(now.minusDays(1))
			.build();

		AuditLogEntity auditLogWrongOperation = AuditLogEntity.builder()
			.originatorId("originatorId1")
			.operationCategory(8)
			.operationAction(3)
			.operationSubject("name")
			.dataJson("dataJson")
			.creationTime(now.minusDays(1))
			.build();

		AuditLogEntity auditLogWrongOriginator = AuditLogEntity.builder()
			.originatorId("originatorId2")
			.operationCategory(8)
			.operationAction(3)
			.operationSubject("name")
			.dataJson("dataJson")
			.creationTime(now.minusDays(1))
			.build();

		Set<AuditLogEntity> entitiesInBorder = stream(auditLogRepository.saveAll(Set.of(auditLog, auditLog1, auditLog2)).spliterator(), false).collect(Collectors.toSet());
		auditLogRepository.saveAll(Set.of(auditLogWrongDate, auditLogWrongDate1, auditLogWrongAction, auditLogWrongOperation, auditLogWrongOriginator));
		Set<AuditLogEntity> found = auditLogRepository.findByCreationTimeBetweenAndOperationActionInAndOperationCategoryInAndOperationSubjectContainingAndOriginatorIdInOrOriginatorPersistenceIdIn(
			now.minusDays(3), now.plusDays(3), Set.of(1, 3, 4), Set.of(1, 2, 5), "", Set.of("originatorId", "originatorId1"), Set.of()
		);

		assertEquals(entitiesInBorder, found);
	}
}