/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.generic_groups;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.audit_log.AuditLogServicePublicator;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.generic_groups.GenericGroupRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {GenericGroupAuditLogService.class, AuditLogServicePublicator.class})
class GenericGroupAuditLogServiceIntegrationTest {
	@MockBean
	private GenericGroupRepository genericGroupRepository;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private Clock clock;
	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private GenericGroupServiceImpl service;

	@BeforeEach
	void setUp() {
		Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		service = new GenericGroupServiceImpl(genericGroupRepository, usersDAO, fixedClock, publisher);
	}

	@Test
	void shouldDetectGroupDeletion() {
		GenericGroupId groupId = new GenericGroupId(UUID.randomUUID());
		when(genericGroupRepository.existsBy("communityId", groupId)).thenReturn(true);
		GenericGroup genericGroup = GenericGroup.builder()
			.id(groupId)
			.build();
		when(genericGroupRepository.findBy(groupId)).thenReturn(Optional.of(genericGroup));

		service.delete("communityId", groupId);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.GENERIC_GROUPS_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectGroupUpdate() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		GenericGroup genericGroup = GenericGroup.builder()
			.id(genericGroupId)
			.communityId("communityId")
			.name("name")
			.description("description")
			.build();
		when(genericGroupRepository.findBy(genericGroupId)).thenReturn(Optional.of(genericGroup));

		service.update(genericGroup);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.GENERIC_GROUPS_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectGroupCreation() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		GenericGroup genericGroup = GenericGroup.builder()
			.id(genericGroupId)
			.communityId("communityId")
			.name("name")
			.description("description")
			.build();

		when(genericGroupRepository.create(genericGroup)).thenReturn(genericGroupId);
		when(genericGroupRepository.findBy(genericGroupId)).thenReturn(Optional.of(genericGroup));

		service.create(genericGroup);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.GENERIC_GROUPS_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}

	@Test
	void shouldDetectUserGroupAssignment() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("fenixUserId");

		when(genericGroupRepository.existsBy("communityId", genericGroupId)).thenReturn(true);
		when(genericGroupRepository.existsBy(genericGroupId, userId)).thenReturn(false);
		when(genericGroupRepository.findBy(genericGroupId)).thenReturn(Optional.of(GenericGroup.builder()
			.id(genericGroupId)
			.build()));
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.id(new PersistentId("id"))
			.email("email")
			.build()));

		service.createMembership("communityId", genericGroupId, userId);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.GENERIC_GROUPS_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.GRANT, argument.getValue().action);
	}

	@Test
	void shouldDetectUserGroupRevoke() {
		GenericGroupId groupId = new GenericGroupId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("userId");

		when(genericGroupRepository.existsBy("communityId", groupId)).thenReturn(true);
		when(genericGroupRepository.findBy(groupId)).thenReturn(Optional.of(GenericGroup.builder()
			.id(groupId)
			.build()));
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.id(new PersistentId("id"))
			.email("email")
			.build()));

		service.deleteMembership("communityId", groupId, userId);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.GENERIC_GROUPS_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.REVOKE, argument.getValue().action);
	}
}
