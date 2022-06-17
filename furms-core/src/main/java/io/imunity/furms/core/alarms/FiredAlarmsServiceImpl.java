/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.alarms;

import io.imunity.furms.api.alarms.FiredAlarmsService;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.alarms.AlarmRemovedEvent;
import io.imunity.furms.domain.alarms.AlarmUpdatedEvent;
import io.imunity.furms.domain.alarms.UserActiveAlarm;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_usage.ResourceUsageUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.spi.alarms.AlarmRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
class FiredAlarmsServiceImpl implements FiredAlarmsService {
	private final AlarmRepository alarmRepository;
	private final AuthzService authzService;
	private final AlarmNotificationService alarmNotificationService;
	private final ResourceUsageRepository resourceUsageRepository;
	private final ProjectAllocationRepository projectAllocationRepository;


	FiredAlarmsServiceImpl(AlarmRepository alarmRepository, AuthzService authzService,
	                       AlarmNotificationService alarmNotificationService,
	                       ResourceUsageRepository resourceUsageRepository, ProjectAllocationRepository projectAllocationRepository) {
		this.alarmRepository = alarmRepository;
		this.authzService = authzService;
		this.alarmNotificationService = alarmNotificationService;
		this.resourceUsageRepository = resourceUsageRepository;
		this.projectAllocationRepository = projectAllocationRepository;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public Set<UserActiveAlarm> findAllFiredAlarmsOfCurrentUser() {
		FURMSUser currentUser = authzService.getCurrentAuthNUser();
		if(currentUser.fenixUserId.isEmpty())
			return Set.of();
		Map<ResourceId, Set<Role>> roles = authzService.getRoles();
		List<ProjectId> projectIds = roles.entrySet().stream()
			.filter(e -> e.getValue().contains(Role.PROJECT_ADMIN))
			.map(e -> e.getKey().asProjectId())
			.collect(toList());

		return alarmRepository.findAll(projectIds, currentUser.fenixUserId.get()).stream()
			.map(activeAlarm -> new UserActiveAlarm(activeAlarm, currentUser.fenixUserId.get(),
				roles.get(new ResourceId(activeAlarm.projectId, PROJECT))))
			.collect(toSet());
	}

	boolean isExceedThreshold(ProjectAllocationId projectAllocationId, int alarmThreshold) {
		BigDecimal cumulativeConsumption = resourceUsageRepository.findCurrentResourceUsage(projectAllocationId)
			.map(resourceUsage -> resourceUsage.cumulativeConsumption)
			.orElse(BigDecimal.ZERO);
		BigDecimal amount = projectAllocationRepository.findById(projectAllocationId).get().amount;
		BigDecimal percentage = percentage(amount, BigDecimal.valueOf(alarmThreshold));
		return percentage.compareTo(cumulativeConsumption) <= 0;
	}

	@EventListener
	void onResourceUsageUpdatedEvent(ResourceUsageUpdatedEvent event){
		alarmRepository.find(event.projectAllocationId)
			.filter(alarm -> !alarm.fired)
			.ifPresent(alarm -> {
				BigDecimal percentage = percentage(event.amount, BigDecimal.valueOf(alarm.threshold));
				if (percentage.compareTo(event.cumulativeConsumption) <= 0) {
					alarmNotificationService.sendNotification(alarm);
					alarmRepository.updateToFired(alarm);
				}
			});
	}

	@EventListener
	void onAlarmRemovedEvent(AlarmRemovedEvent event){
		alarmNotificationService.cleanNotification(event.alarm);
	}

	@EventListener
	void onAlarmRemovedEvent(AlarmUpdatedEvent event){
		if(event.oldAlarm.fired && !event.newAlarm.fired)
			alarmNotificationService.cleanNotification(event.newAlarm);
	}

	private static BigDecimal percentage(BigDecimal base, BigDecimal pct){
		return BigDecimal.valueOf(base.doubleValue() / 100).multiply(pct);
	}
}
