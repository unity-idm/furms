/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.audit_log;


import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class AuditLogDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private AuditLogEntityRepository auditLogRepository;
	@Autowired
	private AuditLogRepository repository;
	@Autowired
	private UsersDAO usersDAO;

	@Test
	void shouldCreate() {
		LocalDateTime now = LocalDate.now().atStartOfDay();
		AuditLog auditLog = AuditLog.builder()
			.originator(FURMSUser.builder()
				.email("email")
				.fenixUserId(new FenixUserId("id"))
				.build())
			.operationCategory(Operation.RESOURCE_CREDIT)
			.action(Action.CREATE)
			.operationSubject("name")
			.dataJson("dataJson")
			.utcTimestamp(now)
			.build();

		repository.create(auditLog);
		Set<AuditLogEntity> auditLogEntities = stream(auditLogRepository.findAll().spliterator(), false)
			.collect(Collectors.toSet());

		assertEquals(1, auditLogEntities.size());
		AuditLogEntity auditLogEntity = auditLogEntities.iterator().next();
		assertEquals("id", auditLogEntity.originatorId);
		assertEquals(1, auditLogEntity.operationCategory);
		assertEquals(2, auditLogEntity.operationAction);
		assertEquals("name", auditLogEntity.operationSubject);
		assertEquals("dataJson", auditLogEntity.dataJson);
		assertEquals(now, auditLogEntity.creationTime);
	}

	@Test
	void shouldFoundIfAllParametersAreSet() {
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
			.operationSubject("name1")
			.dataJson("dataJson1")
			.creationTime(now.plusDays(1))
			.build();

		AuditLogEntity auditLog11 = AuditLogEntity.builder()
			.originatorPersistenceId("originatorPersistenceId")
			.operationCategory(2)
			.operationAction(3)
			.operationSubject("name1")
			.dataJson("dataJson1")
			.creationTime(now.plusDays(1))
			.build();

		AuditLogEntity auditLog2 = AuditLogEntity.builder()
			.originatorId("originatorId")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name2")
			.dataJson("dataJson2")
			.creationTime(now.minusDays(1))
			.build();

		AuditLogEntity auditLog21 = AuditLogEntity.builder()
			.originatorPersistenceId("originatorPersistenceId1")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name2")
			.dataJson("dataJson2")
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

		auditLogRepository.saveAll(Set.of(auditLog, auditLog1, auditLog11, auditLog2, auditLog21, auditLogWrongDate, auditLogWrongDate1, auditLogWrongAction, auditLogWrongOperation, auditLogWrongOriginator));
		FURMSUser user = FURMSUser.builder()
			.email("email")
			.id(new PersistentId("originatorPersistenceId"))
			.fenixUserId(new FenixUserId("originatorId"))
			.build();
		FURMSUser user1 = FURMSUser.builder()
			.email("email1")
			.id(new PersistentId("originatorPersistenceId1"))
			.fenixUserId(new FenixUserId("originatorId1"))
			.build();
		when(usersDAO.getAllUsers()).thenReturn(List.of(
			user,
			user1,
			FURMSUser.builder()
				.email("email2")
				.fenixUserId(new FenixUserId("originatorId2"))
				.build()
		));

		Set<AuditLog> found = repository.findBy(
			now.minusDays(3), now.plusDays(3), Set.of(user, user1), Set.of(1, 3, 4), Set.of(1, 2, 5), ""
		);

		assertEquals(Set.of("originatorId", "originatorId1"), found.stream().map(a -> a.originator.fenixUserId.get().id).collect(Collectors.toSet()));
		assertEquals(Set.of("originatorPersistenceId", "originatorPersistenceId1"), found.stream().map(a -> a.originator.id.get().id).collect(Collectors.toSet()));
		assertEquals(Set.of("dataJson", "dataJson1", "dataJson2"), found.stream().map(a -> a.dataJson).collect(Collectors.toSet()));
		assertEquals(Set.of("name", "name1", "name2"), found.stream().map(a -> a.operationSubject).collect(Collectors.toSet()));
		assertEquals(Set.of(Action.LOGOUT, Action.UPDATE, Action.DELETE), found.stream().map(a -> a.action).collect(Collectors.toSet()));
		assertEquals(Set.of(Operation.RESOURCE_CREDIT, Operation.COMMUNITY_ALLOCATION, Operation.COMMUNITIES_MANAGEMENT), found.stream().map(a -> a.operationCategory).collect(Collectors.toSet()));
		assertEquals(Set.of(now, now.minusDays(1), now.plusDays(1)), found.stream().map(a -> a.utcTimestamp).collect(Collectors.toSet()));
	}

	@Test
	void shouldFoundIfAllActionParameterIsNotSet() {
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
			.operationSubject("name1")
			.dataJson("dataJson1")
			.creationTime(now.plusDays(1))
			.build();

		AuditLogEntity auditLog11 = AuditLogEntity.builder()
			.originatorPersistenceId("originatorPersistenceId")
			.operationCategory(2)
			.operationAction(3)
			.operationSubject("name1")
			.dataJson("dataJson1")
			.creationTime(now.plusDays(1))
			.build();

		AuditLogEntity auditLog2 = AuditLogEntity.builder()
			.originatorId("originatorId")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name2")
			.dataJson("dataJson2")
			.creationTime(now.minusDays(1))
			.build();

		AuditLogEntity auditLog21 = AuditLogEntity.builder()
			.originatorPersistenceId("originatorPersistenceId1")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name2")
			.dataJson("dataJson2")
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

		auditLogRepository.saveAll(Set.of(auditLog, auditLog1, auditLog11, auditLog2, auditLog21, auditLogWrongDate, auditLogWrongDate1, auditLogWrongAction, auditLogWrongOperation, auditLogWrongOriginator));
		FURMSUser user = FURMSUser.builder()
			.email("email")
			.id(new PersistentId("originatorPersistenceId"))
			.fenixUserId(new FenixUserId("originatorId"))
			.build();
		FURMSUser user1 = FURMSUser.builder()
			.email("email1")
			.id(new PersistentId("originatorPersistenceId1"))
			.fenixUserId(new FenixUserId("originatorId1"))
			.build();
		when(usersDAO.getAllUsers()).thenReturn(List.of(
			user,
			user1,
			FURMSUser.builder()
				.email("email2")
				.fenixUserId(new FenixUserId("originatorId2"))
				.build()
		));

		Set<AuditLog> found = repository.findBy(
			now.minusDays(3), now.plusDays(3), Set.of(user, user1), Set.of(), Set.of(1, 2, 5), ""
		);

		assertEquals(Set.of("originatorId", "originatorId1"), found.stream().map(a -> a.originator.fenixUserId.get().id).collect(Collectors.toSet()));
		assertEquals(Set.of("originatorPersistenceId", "originatorPersistenceId1"), found.stream().map(a -> a.originator.id.get().id).collect(Collectors.toSet()));
		assertEquals(Set.of("dataJson", "dataJson1", "dataJson2"), found.stream().map(a -> a.dataJson).collect(Collectors.toSet()));
		assertEquals(Set.of("name", "name1", "name2"), found.stream().map(a -> a.operationSubject).collect(Collectors.toSet()));
		assertEquals(Set.of(Action.LOGOUT, Action.UPDATE, Action.DELETE, Action.ACCEPT), found.stream().map(a -> a.action).collect(Collectors.toSet()));
		assertEquals(Set.of(Operation.RESOURCE_CREDIT, Operation.COMMUNITY_ALLOCATION, Operation.COMMUNITIES_MANAGEMENT), found.stream().map(a -> a.operationCategory).collect(Collectors.toSet()));
		assertEquals(Set.of(now, now.minusDays(1), now.plusDays(1)), found.stream().map(a -> a.utcTimestamp).collect(Collectors.toSet()));
	}

	@Test
	void shouldFoundIfOperationParameterIsNotSet() {
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
			.operationSubject("name1")
			.dataJson("dataJson1")
			.creationTime(now.plusDays(1))
			.build();

		AuditLogEntity auditLog11 = AuditLogEntity.builder()
			.originatorPersistenceId("originatorPersistenceId")
			.operationCategory(2)
			.operationAction(3)
			.operationSubject("name1")
			.dataJson("dataJson1")
			.creationTime(now.plusDays(1))
			.build();

		AuditLogEntity auditLog2 = AuditLogEntity.builder()
			.originatorId("originatorId")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name2")
			.dataJson("dataJson2")
			.creationTime(now.minusDays(1))
			.build();

		AuditLogEntity auditLog21 = AuditLogEntity.builder()
			.originatorPersistenceId("originatorPersistenceId1")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name2")
			.dataJson("dataJson2")
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

		auditLogRepository.saveAll(Set.of(auditLog, auditLog1, auditLog11, auditLog2, auditLog21, auditLogWrongDate, auditLogWrongDate1, auditLogWrongAction, auditLogWrongOperation, auditLogWrongOriginator));
		FURMSUser user = FURMSUser.builder()
			.email("email")
			.id(new PersistentId("originatorPersistenceId"))
			.fenixUserId(new FenixUserId("originatorId"))
			.build();
		FURMSUser user1 = FURMSUser.builder()
			.email("email1")
			.id(new PersistentId("originatorPersistenceId1"))
			.fenixUserId(new FenixUserId("originatorId1"))
			.build();
		when(usersDAO.getAllUsers()).thenReturn(List.of(
			user,
			user1,
			FURMSUser.builder()
				.email("email2")
				.fenixUserId(new FenixUserId("originatorId2"))
				.build()
		));

		Set<AuditLog> found = repository.findBy(
			now.minusDays(3), now.plusDays(3), Set.of(user, user1), Set.of(1, 3, 4), Set.of(), "am"
		);

		assertEquals(Set.of("originatorId", "originatorId1"), found.stream().map(a -> a.originator.fenixUserId.get().id).collect(Collectors.toSet()));
		assertEquals(Set.of("originatorPersistenceId", "originatorPersistenceId1"), found.stream().map(a -> a.originator.id.get().id).collect(Collectors.toSet()));
		assertEquals(Set.of("dataJson", "dataJson1", "dataJson2"), found.stream().map(a -> a.dataJson).collect(Collectors.toSet()));
		assertEquals(Set.of("name", "name1", "name2"), found.stream().map(a -> a.operationSubject).collect(Collectors.toSet()));
		assertEquals(Set.of(Action.LOGOUT, Action.UPDATE, Action.DELETE), found.stream().map(a -> a.action).collect(Collectors.toSet()));
		assertEquals(Set.of(Operation.RESOURCE_CREDIT, Operation.COMMUNITY_ALLOCATION, Operation.COMMUNITIES_MANAGEMENT, Operation.SERVICES_MANAGEMENT), found.stream().map(a -> a.operationCategory).collect(Collectors.toSet()));
		assertEquals(Set.of(now, now.minusDays(1), now.plusDays(1)), found.stream().map(a -> a.utcTimestamp).collect(Collectors.toSet()));
	}

	@Test
	void shouldFoundIfUsersParameterIsNotSet() {
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
			.operationSubject("name1")
			.dataJson("dataJson1")
			.creationTime(now.plusDays(1))
			.build();

		AuditLogEntity auditLog11 = AuditLogEntity.builder()
			.originatorPersistenceId("originatorPersistenceId")
			.operationCategory(2)
			.operationAction(3)
			.operationSubject("name1")
			.dataJson("dataJson1")
			.creationTime(now.plusDays(1))
			.build();

		AuditLogEntity auditLog2 = AuditLogEntity.builder()
			.originatorId("originatorId")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name2")
			.dataJson("dataJson2")
			.creationTime(now.minusDays(1))
			.build();

		AuditLogEntity auditLog21 = AuditLogEntity.builder()
			.originatorPersistenceId("originatorPersistenceId1")
			.operationCategory(5)
			.operationAction(4)
			.operationSubject("name2")
			.dataJson("dataJson2")
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

		auditLogRepository.saveAll(Set.of(auditLog, auditLog1, auditLog11, auditLog2, auditLog21, auditLogWrongDate, auditLogWrongDate1, auditLogWrongAction, auditLogWrongOperation, auditLogWrongOriginator));
		FURMSUser user = FURMSUser.builder()
			.email("email")
			.id(new PersistentId("originatorPersistenceId"))
			.fenixUserId(new FenixUserId("originatorId"))
			.build();
		FURMSUser user1 = FURMSUser.builder()
			.email("email1")
			.id(new PersistentId("originatorPersistenceId1"))
			.fenixUserId(new FenixUserId("originatorId1"))
			.build();
		when(usersDAO.getAllUsers()).thenReturn(List.of(
			user,
			user1,
			FURMSUser.builder()
				.email("email2")
				.fenixUserId(new FenixUserId("originatorId2"))
				.build()
		));

		Set<AuditLog> found = repository.findBy(
			now.minusDays(3), now.plusDays(3), Set.of(), Set.of(1, 3, 4), Set.of(1, 2, 5), ""
		);

		assertEquals(Set.of("originatorId", "originatorId1"), found.stream().map(a -> a.originator.fenixUserId.get().id).collect(Collectors.toSet()));
		assertEquals(Set.of("originatorPersistenceId", "originatorPersistenceId1"), found.stream().map(a -> a.originator.id.get().id).collect(Collectors.toSet()));
		assertEquals(Set.of("dataJson", "dataJson1", "dataJson2"), found.stream().map(a -> a.dataJson).collect(Collectors.toSet()));
		assertEquals(Set.of("name", "name1", "name2"), found.stream().map(a -> a.operationSubject).collect(Collectors.toSet()));
		assertEquals(Set.of(Action.LOGOUT, Action.UPDATE, Action.DELETE), found.stream().map(a -> a.action).collect(Collectors.toSet()));
		assertEquals(Set.of(Operation.RESOURCE_CREDIT, Operation.COMMUNITY_ALLOCATION, Operation.COMMUNITIES_MANAGEMENT), found.stream().map(a -> a.operationCategory).collect(Collectors.toSet()));
		assertEquals(Set.of(now, now.minusDays(1), now.plusDays(1)), found.stream().map(a -> a.utcTimestamp).collect(Collectors.toSet()));
	}
}