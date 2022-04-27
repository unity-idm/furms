/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import io.imunity.furms.api.validation.exceptions.AlarmAlreadyExceedThresholdException;
import io.imunity.furms.api.validation.exceptions.AlarmNotExistingException;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.domain.alarms.AlarmCreatedEvent;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmRemovedEvent;
import io.imunity.furms.domain.alarms.AlarmUpdatedEvent;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.alarms.AlarmRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlarmServiceImplTest {

	@Mock
	private AlarmRepository alarmRepository;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private FiredAlarmsServiceImpl firedAlarmsService;
	@Mock
	private ApplicationEventPublisher publisher;

	@InjectMocks
	private AlarmServiceImpl alarmService;

	@Test
	void shouldFindAll() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId1 = new ProjectAllocationId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("id");
		FenixUserId fenixUserId1 = new FenixUserId("id1");
		FenixUserId fenixUserId2 = new FenixUserId("id2");
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
				.build())
		);
		when(alarmRepository.findAll(projectId)).thenReturn(Set.of(
			AlarmWithUserIds.builder()
				.id(new AlarmId(UUID.randomUUID()))
				.name("name")
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.allUsers(true)
				.alarmUser(Set.of(fenixUserId, fenixUserId1))
				.build(),
			AlarmWithUserIds.builder()
				.id(new AlarmId(UUID.randomUUID()))
				.name("name1")
				.projectId(projectId)
				.projectAllocationId(projectAllocationId1)
				.allUsers(true)
				.alarmUser(Set.of(fenixUserId2))
				.build()
			));

		Set<AlarmWithUserEmails> alarms = alarmService.findAll(projectId);
		assertThat(alarms.stream().flatMap(alarm -> alarm.alarmUserEmails.stream()).collect(toSet())).isEqualTo(Set.of("email", "email1", "email2"));
		assertThat(alarms.stream().map(alarm -> alarm.name).collect(toSet())).isEqualTo(Set.of("name", "name1"));
		assertThat(alarms.stream().map(alarm -> alarm.projectAllocationId).collect(toSet())).isEqualTo(Set.of(projectAllocationId,
			projectAllocationId1));
	}

	@Test
	void shouldFind() {
		AlarmId alarmId = new AlarmId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("id");
		FenixUserId fenixUserId1 = new FenixUserId("id1");
		when(usersDAO.getAllUsers()).thenReturn(List.of(
			FURMSUser.builder()
				.fenixUserId(fenixUserId)
				.email("email")
				.build(),
			FURMSUser.builder()
				.fenixUserId(fenixUserId1)
				.email("email1")
				.build()
		));
		when(alarmRepository.find(alarmId)).thenReturn(Optional.of(
			AlarmWithUserIds.builder()
				.id(alarmId)
				.name("name")
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.allUsers(true)
				.alarmUser(Set.of(fenixUserId, fenixUserId1))
				.build()
		));
		when(alarmRepository.exist(projectId, alarmId)).thenReturn(true);

		Optional<AlarmWithUserEmails> alarmWithUserEmails = alarmService.find(projectId, alarmId);
		assertThat(alarmWithUserEmails).isPresent();
		assertThat(alarmWithUserEmails.get().id).isEqualTo(alarmId);
		assertThat(alarmWithUserEmails.get().name).isEqualTo("name");
		assertThat(alarmWithUserEmails.get().projectId).isEqualTo(projectId);
		assertThat(alarmWithUserEmails.get().projectAllocationId).isEqualTo(projectAllocationId);
		assertThat(alarmWithUserEmails.get().allUsers).isEqualTo(true);
		assertThat(alarmWithUserEmails.get().alarmUserEmails).isEqualTo(Set.of("email", "email1"));
	}

	@Test
	void shouldCreate() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		int threshold = 50;
		AlarmId alarmId = new AlarmId(UUID.randomUUID());

		FenixUserId fenixUserId = new FenixUserId("id");
		FenixUserId fenixUserId1 = new FenixUserId("id1");
		when(usersDAO.getAllUsers()).thenReturn(List.of(
			FURMSUser.builder()
				.fenixUserId(fenixUserId)
				.email("email")
				.build(),
			FURMSUser.builder()
				.fenixUserId(fenixUserId1)
				.email("email1")
				.build()
		));
		when(alarmRepository.exist(projectId, "name")).thenReturn(false);

		AlarmWithUserIds alarmWithUserIds = AlarmWithUserIds.builder()
			.id(new AlarmId((UUID) null))
			.name("name")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.allUsers(true)
			.threshold(threshold)
			.alarmUser(Set.of(fenixUserId, fenixUserId1))
			.build();
		when(alarmRepository.create(alarmWithUserIds)).thenReturn(alarmId);
		when(alarmRepository.find(alarmId)).thenReturn(Optional.of(alarmWithUserIds));
		when(firedAlarmsService.isExceedThreshold(projectAllocationId, threshold)).thenReturn(false);


		alarmService.create(AlarmWithUserEmails.builder()
			.id(new AlarmId((UUID) null))
			.name("name")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.allUsers(true)
			.threshold(threshold)
			.alarmUser(Set.of("email", "email1"))
			.build()
		);

		verify(alarmRepository).create(alarmWithUserIds);
		verify(publisher).publishEvent(new AlarmCreatedEvent(alarmWithUserIds));
	}

	@Test
	void shouldNotCreateWhenThresholdExceedsUsage() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		int threshold = 50;

		when(alarmRepository.exist(projectId, "name")).thenReturn(false);
		when(firedAlarmsService.isExceedThreshold(projectAllocationId, threshold)).thenReturn(true);

		assertThrows(AlarmAlreadyExceedThresholdException.class, () ->
			alarmService.create(AlarmWithUserEmails.builder()
				.id(new AlarmId((UUID) null))
				.name("name")
				.projectId(projectId)
				.projectAllocationId(projectAllocationId)
				.allUsers(true)
				.threshold(threshold)
				.alarmUser(Set.of("email", "email1"))
				.build()
			));
	}

	@Test
	void shouldNotCreateWhenNameIsDuplicated() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		AlarmId alarmId = new AlarmId(UUID.randomUUID());

		when(alarmRepository.exist(projectId, "name")).thenReturn(true);

		assertThrows(DuplicatedNameValidationError.class, () ->
			alarmService.create(AlarmWithUserEmails.builder()
				.id(alarmId)
				.name("name")
				.projectId(projectId)
				.projectAllocationId(UUID.randomUUID().toString())
				.allUsers(true)
				.alarmUser(Set.of("email", "email1"))
				.build())
		);

	}

	@Test
	void shouldUpdateWitUnsettingAlarmToFired() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		int threshold = 50;
		AlarmId alarmId = new AlarmId(UUID.randomUUID());

		FenixUserId fenixUserId = new FenixUserId("id");
		FenixUserId fenixUserId1 = new FenixUserId("id1");
		AlarmWithUserIds oldAlarm = AlarmWithUserIds.builder()
			.name("name")
			.fired(true)
			.build();
		when(usersDAO.getAllUsers()).thenReturn(List.of(
			FURMSUser.builder()
				.fenixUserId(fenixUserId)
				.email("email")
				.build(),
			FURMSUser.builder()
				.fenixUserId(fenixUserId1)
				.email("email1")
				.build()
		));
		when(alarmRepository.exist(projectId, alarmId)).thenReturn(true);
		when(alarmRepository.find(alarmId)).thenReturn(Optional.of(oldAlarm));
		when(firedAlarmsService.isExceedThreshold(projectAllocationId, threshold)).thenReturn(false);

		alarmService.update(AlarmWithUserEmails.builder()
			.id(alarmId)
			.name("name")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.allUsers(true)
			.threshold(threshold)
			.fired(true)
			.alarmUser(Set.of("email", "email1"))
			.build()
		);

		AlarmWithUserIds alarmWithUserIds = AlarmWithUserIds.builder()
			.id(alarmId)
			.name("name")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.allUsers(true)
			.threshold(threshold)
			.fired(false)
			.alarmUser(Set.of(fenixUserId, fenixUserId1))
			.build();

		verify(alarmRepository).update(alarmWithUserIds);
		verify(publisher).publishEvent(new AlarmUpdatedEvent(alarmWithUserIds, oldAlarm));
	}

	@Test
	void shouldUpdateWithSettingAlarmToFired() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		AlarmId alarmId = new AlarmId(UUID.randomUUID());

		FenixUserId fenixUserId = new FenixUserId("id");
		FenixUserId fenixUserId1 = new FenixUserId("id1");
		AlarmWithUserIds oldAlarm = AlarmWithUserIds.builder()
			.name("name")
			.build();
		when(usersDAO.getAllUsers()).thenReturn(List.of(
			FURMSUser.builder()
				.fenixUserId(fenixUserId)
				.email("email")
				.build(),
			FURMSUser.builder()
				.fenixUserId(fenixUserId1)
				.email("email1")
				.build()
		));
		when(alarmRepository.exist(projectId, alarmId)).thenReturn(true);
		when(alarmRepository.find(alarmId)).thenReturn(Optional.of(oldAlarm));

		alarmService.update(AlarmWithUserEmails.builder()
			.id(alarmId)
			.name("name")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.allUsers(true)
			.alarmUser(Set.of("email", "email1"))
			.build());

		AlarmWithUserIds alarmWithUserIds = AlarmWithUserIds.builder()
			.id(alarmId)
			.name("name")
			.projectId(projectId)
			.projectAllocationId(projectAllocationId)
			.allUsers(true)
			.alarmUser(Set.of(fenixUserId, fenixUserId1))
			.build();

		verify(alarmRepository).update(alarmWithUserIds);
		verify(publisher).publishEvent(new AlarmUpdatedEvent(alarmWithUserIds, oldAlarm));
	}

	@Test
	void shouldNotUpdateIfProjectIdAndAlarmIdNotConnected() {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		AlarmId alarmId = new AlarmId(UUID.randomUUID());

		when(alarmRepository.exist(projectId, alarmId)).thenReturn(false);

		assertThrows(AlarmNotExistingException.class, () ->
			alarmService.update(AlarmWithUserEmails.builder()
				.id(alarmId)
				.name("name")
				.projectId(projectId)
				.projectAllocationId(UUID.randomUUID().toString())
				.allUsers(true)
				.alarmUser(Set.of("email", "email1"))
				.build())
		);
	}

	@Test
	void shouldRemove() {
		AlarmId alarmId = new AlarmId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		AlarmWithUserIds alarmWithUserIds = AlarmWithUserIds.builder()
			.name("name")
			.build();

		when(alarmRepository.find(alarmId)).thenReturn(Optional.of(alarmWithUserIds));
		when(alarmRepository.exist(projectId, alarmId)).thenReturn(true);

		alarmService.remove(projectId, alarmId);

		verify(alarmRepository).remove(alarmId);
		verify(publisher).publishEvent(new AlarmRemovedEvent(alarmWithUserIds));
	}

	@Test
	void shouldNotRemoveIfProjectIdAndAlarmIdNotConnected() {
		AlarmId alarmId = new AlarmId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());

		when(alarmRepository.exist(projectId, alarmId)).thenReturn(false);

		assertThrows(AlarmNotExistingException.class, () -> alarmService.remove(projectId, alarmId));
	}
}
