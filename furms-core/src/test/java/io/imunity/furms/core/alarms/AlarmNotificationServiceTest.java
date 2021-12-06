/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.notification.UserAlarmListChangedEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlarmNotificationServiceTest {

	@Mock
	private ProjectGroupsDAO projectGroupsDAO;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;
	@Mock
	private ResourceUsageRepository resourceUsageRepository;
	@Mock
	private EmailNotificationSender emailNotificationSender;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;

	@InjectMocks
	private AlarmNotificationService alarmNotificationService;

	@BeforeEach
	void setUp() {
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void clear() {
		TransactionSynchronizationManager.clear();
	}

	void commit() {
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}
	}

	@Test
	void shouldNotifyUsers() {
		String communityId = "communityId";
		String projectId = "projectId";
		String projectAllocationId = "projectAllocationId";
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
		commit();

		verify(emailNotificationSender).notifyProjectAdminAboutResourceUsage(userId, projectId, projectAllocationId, "projectAllocationName", "alarmName");
		verify(emailNotificationSender).notifyProjectUserAboutResourceUsage(userId1, projectId, projectAllocationId, "projectAllocationName", "alarmName");
		verify(emailNotificationSender).notifyUserAboutResourceUsage(userId2, projectId, projectAllocationId, "projectAllocationName", "alarmName");
		verify(publisher).publishEvent(new UserAlarmListChangedEvent(fenixUserId));
		verify(publisher).publishEvent(new UserAlarmListChangedEvent(fenixUserId1));
		verify(publisher).publishEvent(new UserAlarmListChangedEvent(fenixUserId2));
	}

	@Test
	void shouldCleanNotification(){
		String communityId = "communityId";
		String projectId = "projectId";
		String projectAllocationId = "projectAllocationId";
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
		commit();

		verify(publisher).publishEvent(new UserAlarmListChangedEvent(fenixUserId));
		verify(publisher).publishEvent(new UserAlarmListChangedEvent(fenixUserId1));
		verify(publisher).publishEvent(new UserAlarmListChangedEvent(fenixUserId2));

	}

}
