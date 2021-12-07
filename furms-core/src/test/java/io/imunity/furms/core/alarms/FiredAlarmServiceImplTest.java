/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.alarms.FiredAlarm;
import io.imunity.furms.domain.alarms.UserActiveAlarm;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.ResourceUsageUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.alarms.AlarmRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiredAlarmServiceImplTest {

	@Mock
	private AlarmRepository alarmRepository;
	@Mock
	private AuthzService authzService;
	@Mock
	private AlarmNotificationService alarmNotificationService;
	@Mock
	private ResourceUsageRepository resourceUsageRepository;
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;

	@InjectMocks
	private FiredAlarmsServiceImpl firedAlarmsService;

	@Test
	void shouldFindAllActiveAlarmsOffCurrentUser() {
		AlarmId alarmId = new AlarmId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("userId");
		UUID projectId = UUID.randomUUID();
		when(authzService.getCurrentAuthNUser()).thenReturn(FURMSUser.builder()
			.fenixUserId(userId)
			.email("email")
			.build()
		);
		when(authzService.getRoles()).thenReturn(
			Map.of(new ResourceId(projectId, PROJECT), Set.of(Role.PROJECT_ADMIN))
		);
		FiredAlarm firedAlarm = FiredAlarm.builder()
			.alarmId(alarmId)
			.projectId(projectId.toString())
			.projectAllocationId("projectAllocationId")
			.alarmName("alarmName")
			.projectAllocationName("projectAllocationName")
			.build();
		when(alarmRepository.findAll(List.of(projectId), userId)).thenReturn(Set.of(firedAlarm));

		Set<UserActiveAlarm> allActiveAlarmsOffCurrentUser = firedAlarmsService.findAllFiredAlarmsOfCurrentUser();
		assertThat(allActiveAlarmsOffCurrentUser.size()).isEqualTo(1);
		assertThat(allActiveAlarmsOffCurrentUser.iterator().next()).isEqualTo(new UserActiveAlarm(firedAlarm, userId, Set.of(Role.PROJECT_ADMIN)));
	}

	@Test
	void shouldNotifyUsersAfterResourceUsageUpdatedWhenThresholdIsLessThenUsage() {
		String projectId = "projectId";
		String projectAllocationId = "projectAllocationId";
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		FenixUserId fenixUserId1 = new FenixUserId("fenixUserId1");
		FenixUserId fenixUserId2 = new FenixUserId("fenixUserId2");

		ResourceUsageUpdatedEvent usageUpdatedEvent = new ResourceUsageUpdatedEvent(BigDecimal.TEN, BigDecimal.ONE, projectAllocationId);

		AlarmWithUserIds alarm = AlarmWithUserIds.builder()
			.name("alarmName")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.threshold(9)
			.allUsers(false)
			.alarmUser(Set.of(fenixUserId, fenixUserId1, fenixUserId2))
			.build();
		when(alarmRepository.find(projectAllocationId)).thenReturn(Optional.of(alarm));

		firedAlarmsService.onResourceUsageUpdatedEvent(usageUpdatedEvent);

		verify(alarmNotificationService).sendNotification(alarm);
		verify(alarmRepository).updateToFired(alarm);
	}

	@Test
	void shouldNotifyUsersAfterResourceUsageUpdatedWhenThresholdEqualsUsage() {
		String projectId = "projectId";
		String projectAllocationId = "projectAllocationId";
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		FenixUserId fenixUserId1 = new FenixUserId("fenixUserId1");
		FenixUserId fenixUserId2 = new FenixUserId("fenixUserId2");

		ResourceUsageUpdatedEvent usageUpdatedEvent = new ResourceUsageUpdatedEvent(BigDecimal.TEN, BigDecimal.ONE, projectAllocationId);

		AlarmWithUserIds alarm = AlarmWithUserIds.builder()
			.name("alarmName")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.threshold(10)
			.allUsers(false)
			.alarmUser(Set.of(fenixUserId, fenixUserId1, fenixUserId2))
			.build();
		when(alarmRepository.find(projectAllocationId)).thenReturn(Optional.of(alarm));

		firedAlarmsService.onResourceUsageUpdatedEvent(usageUpdatedEvent);

		verify(alarmNotificationService).sendNotification(alarm);
		verify(alarmRepository).updateToFired(alarm);
	}

	@Test
	void shouldNotNotifyUsersAfterResourceUsageUpdatedWhenThresholdExceedsUsage() {
		String projectId = "projectId";
		String projectAllocationId = "projectAllocationId";
		FenixUserId fenixUserId = new FenixUserId("fenixUserId");
		FenixUserId fenixUserId1 = new FenixUserId("fenixUserId1");
		FenixUserId fenixUserId2 = new FenixUserId("fenixUserId2");

		ResourceUsageUpdatedEvent usageUpdatedEvent = new ResourceUsageUpdatedEvent(BigDecimal.TEN, BigDecimal.ONE, projectAllocationId);

		AlarmWithUserIds alarm = AlarmWithUserIds.builder()
			.name("alarmName")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.threshold(11)
			.allUsers(false)
			.alarmUser(Set.of(fenixUserId, fenixUserId1, fenixUserId2))
			.build();
		when(alarmRepository.find(projectAllocationId)).thenReturn(Optional.of(alarm));

		firedAlarmsService.onResourceUsageUpdatedEvent(usageUpdatedEvent);

		verify(alarmNotificationService, times(0)).sendNotification(alarm);
		verify(alarmRepository, times(0)).updateToFired(alarm);
	}

	@Test
	void shouldExceedWhenUsageIsBiggerThenThreshold() {
		String projectAllocationId = "projectAllocationId";

		when(resourceUsageRepository.findCurrentResourceUsage(projectAllocationId)).thenReturn(Optional.of(
			ResourceUsage.builder()
				.cumulativeConsumption(new BigDecimal(6))
				.build()
		));
		when(projectAllocationRepository.findById(projectAllocationId)).thenReturn(Optional.of(
			ProjectAllocation.builder()
				.amount(BigDecimal.TEN)
				.name("projectAllocationName")
				.build()
		));

		boolean value = firedAlarmsService.isExceedThreshold(projectAllocationId, 50);
		assertThat(value).isTrue();
	}

	@Test
	void shouldExceedWhenUsageIsEqualThenThreshold() {
		String projectAllocationId = "projectAllocationId";

		when(resourceUsageRepository.findCurrentResourceUsage(projectAllocationId)).thenReturn(Optional.of(
			ResourceUsage.builder()
				.cumulativeConsumption(new BigDecimal(5))
				.build()
		));
		when(projectAllocationRepository.findById(projectAllocationId)).thenReturn(Optional.of(
			ProjectAllocation.builder()
				.amount(BigDecimal.TEN)
				.name("projectAllocationName")
				.build()
		));

		boolean value = firedAlarmsService.isExceedThreshold(projectAllocationId, 50);
		assertThat(value).isTrue();
	}

	@Test
	void shouldNotExceedWhenUsageIsLessThenThreshold() {
		String projectAllocationId = "projectAllocationId";

		when(resourceUsageRepository.findCurrentResourceUsage(projectAllocationId)).thenReturn(Optional.of(
			ResourceUsage.builder()
				.cumulativeConsumption(new BigDecimal(4))
				.build()
		));
		when(projectAllocationRepository.findById(projectAllocationId)).thenReturn(Optional.of(
			ProjectAllocation.builder()
				.amount(BigDecimal.TEN)
				.name("projectAllocationName")
				.build()
		));

		boolean value = firedAlarmsService.isExceedThreshold(projectAllocationId, 50);
		assertThat(value).isFalse();
	}
}
