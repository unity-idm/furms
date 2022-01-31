/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.audit_log.AuditLogServiceImplTest;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.spi.alarms.AlarmRepository;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.notifications.EmailNotificationSender;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {AlarmAuditLogService.class, AuditLogServiceImplTest.class})
class AlarmAuditLogServiceTest {
	@MockBean
	private ProjectAllocationRepository projectAllocationRepository;
	@MockBean
	private AlarmRepository alarmRepository;
	@MockBean
	private FiredAlarmsServiceImpl firedAlarmsService;
	@MockBean
	private AuthzService authzService;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;
	@MockBean
	private ProjectGroupsDAO projectGroupsDAO;
	@MockBean
	private ProjectRepository projectRepository;
	@MockBean
	private EmailNotificationSender emailNotificationSender;
	@MockBean
	private  AlarmNotificationService alarmNotificationService;
	@MockBean
	private ResourceUsageRepository resourceUsageRepository;

	private AlarmServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new AlarmServiceImpl(alarmRepository, usersDAO, firedAlarmsService, projectAllocationRepository, publisher);
	}

	@Test
	void shouldDetectAlarmDeletion() {
		//given
		AlarmId id = new AlarmId(UUID.randomUUID());
		when(alarmRepository.exist("projectId", id)).thenReturn(true);
		when(alarmRepository.find(id)).thenReturn(Optional.of(
			AlarmWithUserIds.builder()
				.id(id)
				.name("userFacingName")
				.build()
		));
		//when
		service.remove("projectId", id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ALARM_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectAlarmUpdate() {
		//given
		AlarmId id = new AlarmId(UUID.randomUUID());
		AlarmWithUserEmails request = AlarmWithUserEmails.builder()
			.id(id)
			.name("userFacingName")
			.projectId("projectId")
			.alarmUser(Set.of())
			.build();
		AlarmWithUserIds response = AlarmWithUserIds.builder()
			.id(id)
			.name("userFacingName")
			.build();
		when(alarmRepository.exist("projectId", id)).thenReturn(true);
		when(alarmRepository.find(id)).thenReturn(Optional.of(response));

		//when
		service.update(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ALARM_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectAlarmCreation() {
		//given
		AlarmId id = new AlarmId(UUID.randomUUID());
		AlarmWithUserEmails request = AlarmWithUserEmails.builder()
			.id(id)
			.projectId("projectId")
			.name("userFacingName")
			.alarmUser(Set.of())
			.build();
		AlarmWithUserIds response = AlarmWithUserIds.builder()
			.id(id)
			.projectId("projectId")
			.name("userFacingName")
			.alarmUser(Set.of())
			.build();
		when(alarmRepository.find(id)).thenReturn(Optional.of(response));
		when(alarmRepository.create(response)).thenReturn(id);

		//when
		service.create(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ALARM_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}
}
