/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.notification.UserAlarmListChangedEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.alarms.AlarmRepository;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.notifications.EmailNotificationSender;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SpringBootLauncher.class)
@ExtendWith(MockitoExtension.class)
@RecordApplicationEvents
class AlarmNotificationServiceTest {

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
	@MockBean
	private AuditLogRepository auditLogRepository;
	@MockBean
	private ProjectGroupsDAO projectGroupsDAO;
	@MockBean
	private ProjectRepository projectRepository;
	@MockBean
	private EmailNotificationSender emailNotificationSender;
	@MockBean
	private ResourceUsageRepository resourceUsageRepository;
	@Autowired
	private ApplicationEvents applicationEvents;
	@Autowired
	private AlarmNotificationService alarmNotificationService;

	@BeforeEach
	void setUp() {
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void clear() {
		TransactionSynchronizationManager.clear();
	}

	@Test
	void shouldNotifyUsers() {
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		FenixUserId fenixUserId1 = new FenixUserId("fenixUserId1");
		FenixUserId fenixUserId2 = new FenixUserId("fenixUserId2");
		PersistentId userId = new PersistentId("userId");
		PersistentId userId1 = new PersistentId("userId1");
		PersistentId userId2 = new PersistentId("userId2");

		AlarmWithUserIds alarm = AlarmWithUserIds.builder()
			.name("alarmName")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.threshold(50)
			.allUsers(false)
			.alarmUser(Set.of(fenixUserId, fenixUserId1, fenixUserId2))
			.build();

		when(projectAllocationRepository.findById(projectAllocationId)).thenReturn(Optional.of(
			ProjectAllocation.builder()
				.amount(BigDecimal.TEN)
				.name("projectAllocationName")
				.build()
		));
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(
			Project.builder()
				.communityId(communityId)
				.build()
		));

		when(usersDAO.getAllUsers()).thenReturn(List.of(
			FURMSUser.builder()
				.id(userId)
				.fenixUserId(fenixUserId)
				.email("email")
				.build(),
			FURMSUser.builder()
				.id(userId1)
				.fenixUserId(fenixUserId1)
				.email("email1")
				.build(),
			FURMSUser.builder()
				.id(userId2)
				.fenixUserId(fenixUserId2)
				.email("email2")
				.build()
		));

		when(projectGroupsDAO.getAllAdmins(communityId, projectId)).thenReturn(List.of(
			FURMSUser.builder()
				.id(userId)
				.fenixUserId(fenixUserId)
				.email("email")
				.build()
		));
		when(projectGroupsDAO.getAllUsers(communityId, projectId)).thenReturn(List.of(
			FURMSUser.builder()
				.id(userId1)
				.fenixUserId(fenixUserId1)
				.email("email1")
				.build()
		));

		alarmNotificationService.sendNotification(alarm);

		verify(emailNotificationSender).notifyProjectAdminAboutResourceUsage(userId, projectId, projectAllocationId, "projectAllocationName", "alarmName");
		verify(emailNotificationSender).notifyProjectUserAboutResourceUsage(userId1, projectId, projectAllocationId, "projectAllocationName", "alarmName");
		verify(emailNotificationSender).notifyUserAboutResourceUsage(userId2, projectId, projectAllocationId, "projectAllocationName", "alarmName");
		List<Object> applicationEvents = getApplicationEvents();

		assertEquals(1,
			applicationEvents.stream().filter(x -> x.equals(new UserAlarmListChangedEvent(fenixUserId))).count()
		);
		assertEquals(1,
			applicationEvents.stream().filter(x -> x.equals(new UserAlarmListChangedEvent(fenixUserId1))).count()
		);
		assertEquals(1,
			applicationEvents.stream().filter(x -> x.equals(new UserAlarmListChangedEvent(fenixUserId2))).count()
		);
	}

	private List<Object> getApplicationEvents() {
		return applicationEvents.stream()
			.filter(x -> x instanceof PayloadApplicationEvent)
			.map(x -> ((PayloadApplicationEvent<?>)x).getPayload())
			.collect(Collectors.toList());
	}

	@Test
	void shouldCleanNotification(){
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		FenixUserId fenixUserId1 = new FenixUserId("fenixUserId1");
		FenixUserId fenixUserId2 = new FenixUserId("fenixUserId2");

		AlarmWithUserIds alarm = AlarmWithUserIds.builder()
		.name("alarmName")
		.projectId(projectId)
		.projectAllocationId(projectAllocationId)
		.threshold(50)
		.allUsers(false)
		.alarmUser(Set.of(fenixUserId, fenixUserId1, fenixUserId2))
		.build();

		when(projectRepository.findById(projectId)).thenReturn(Optional.of(
			Project.builder()
				.communityId(communityId)
				.build()
		));
		when(usersDAO.getAllUsers()).thenReturn(List.of(
			FURMSUser.builder()
				.fenixUserId(fenixUserId)
				.email("email")
				.build(),
			FURMSUser.builder()
				.fenixUserId(fenixUserId1)
				.email("email1")
				.build(),
			FURMSUser.builder()
				.fenixUserId(fenixUserId2)
				.email("email2")
				.build()
		));

		alarmNotificationService.cleanNotification(alarm);

		List<Object> applicationEvents = getApplicationEvents();

		assertEquals(1,
			applicationEvents.stream().filter(x -> x.equals(new UserAlarmListChangedEvent(fenixUserId))).count()
		);
		assertEquals(1,
			applicationEvents.stream().filter(x -> x.equals(new UserAlarmListChangedEvent(fenixUserId1))).count()
		);
		assertEquals(1,
			applicationEvents.stream().filter(x -> x.equals(new UserAlarmListChangedEvent(fenixUserId2))).count()
		);
	}
}
