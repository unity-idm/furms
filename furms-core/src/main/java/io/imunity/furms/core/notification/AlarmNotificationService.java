/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.notification;

import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.alarms.AlarmCreatedEvent;
import io.imunity.furms.domain.alarms.AlarmRemovedEvent;
import io.imunity.furms.domain.alarms.AlarmUpdatedEvent;
import io.imunity.furms.domain.notification.UserAlarmListChangedEvent;
import io.imunity.furms.domain.resource_usage.ResourceUsageUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.alarms.AlarmRepository;
import io.imunity.furms.spi.notifications.EmailNotificationDAO;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
class AlarmNotificationService {

	private final AlarmRepository alarmRepository;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final ProjectRepository projectRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final ResourceUsageRepository resourceUsageRepository;
	private final EmailNotificationDAO emailNotificationDAO;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;

	AlarmNotificationService(AlarmRepository alarmRepository, ProjectGroupsDAO projectGroupsDAO,
	                         ProjectRepository projectRepository, ProjectAllocationRepository projectAllocationRepository,
	                         ResourceUsageRepository resourceUsageRepository, EmailNotificationDAO emailNotificationDAO,
	                         UsersDAO usersDAO, ApplicationEventPublisher publisher) {
		this.alarmRepository = alarmRepository;
		this.projectGroupsDAO = projectGroupsDAO;
		this.projectRepository = projectRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.resourceUsageRepository = resourceUsageRepository;
		this.emailNotificationDAO = emailNotificationDAO;
		this.usersDAO = usersDAO;
		this.publisher = publisher;
	}

	@EventListener
	void onResourceUsageUpdatedEvent(ResourceUsageUpdatedEvent event){
		alarmRepository.find(event.projectAllocationId)
			.ifPresent(alarm -> {
				BigDecimal percentage = percentage(event.amount, BigDecimal.valueOf(alarm.threshold));
				if (percentage.compareTo(event.cumulativeConsumption) < 0) {
					sendNotification(alarm);
				}
			});
	}

	@EventListener
	void onAlarmCreatedEvent(AlarmCreatedEvent event){
		handleNotification(event.alarm);
	}

	@EventListener
	void onAlarmUpdatedEvent(AlarmUpdatedEvent event){
		handleNotification(event.newAlarm);
	}

	@EventListener
	void onAlarmRemovedEvent(AlarmRemovedEvent event){
		handleNotification(event.alarm);
	}

	private void handleNotification(AlarmWithUserIds alarmWithUserIds) {
		BigDecimal cumulativeConsumption = resourceUsageRepository.findCurrentResourceUsage(alarmWithUserIds.projectAllocationId)
			.map(resourceUsage -> resourceUsage.cumulativeConsumption)
			.orElse(BigDecimal.ZERO);
		BigDecimal amount = projectAllocationRepository.findById(alarmWithUserIds.projectAllocationId).get().amount;

		alarmRepository.find(alarmWithUserIds.projectAllocationId)
			.ifPresent(alarm -> {
				BigDecimal percentage = percentage(amount, BigDecimal.valueOf(alarm.threshold));
				if (percentage.compareTo(cumulativeConsumption) >= 0) {
					sendNotification(alarm);
				}
			});
	}

	private void sendNotification(AlarmWithUserIds alarm) {
		String projectAllocationName = projectAllocationRepository.findById(alarm.projectAllocationId).get().name;
		String communityId = projectRepository.findById(alarm.projectId).get().getCommunityId();
		getAlarmUserStream(alarm, communityId)
			.distinct()
			.forEach(y -> {
				emailNotificationDAO.notifyAdminAboutResourceUsage(y.id.get(), alarm.projectId, alarm.projectAllocationId, projectAllocationName, alarm.name);
				publisher.publishEvent(new UserAlarmListChangedEvent(y.fenixUserId.get()));
			});
	}

	private Stream<FURMSUser> getAlarmUserStream(AlarmWithUserIds alarm, String communityId) {
		Map<FenixUserId, FURMSUser> furmsUserMap = usersDAO.getAllUsers().stream()
			.filter(user -> user.fenixUserId.isPresent())
			.collect(Collectors.toMap(user -> user.fenixUserId.get(), Function.identity()));
		Stream<FURMSUser> furmsUserStream = alarm.alarmUser.stream()
			.map(id -> Optional.ofNullable(furmsUserMap.get(id)))
			.filter(Optional::isPresent)
			.map(Optional::get);

		return alarm.allUsers ?
			furmsUserStream :
			Stream.concat(
				projectGroupsDAO.getAllAdmins(communityId, alarm.projectId).stream(),
				furmsUserStream
			);
	}

	private static BigDecimal percentage(BigDecimal base, BigDecimal pct){
		return BigDecimal.valueOf(base.doubleValue() / 100).multiply(pct);
	}

}
