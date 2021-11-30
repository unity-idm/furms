/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;

import io.imunity.furms.api.alarms.ActiveAlarmsService;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.project.allocations.ResourceAllocationsDetailsView;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

@Component
class AlarmNotificationProducer implements NotificationProducer{
	private final ActiveAlarmsService activeAlarmsService;

	AlarmNotificationProducer(ActiveAlarmsService activeAlarmsService) {
		this.activeAlarmsService = activeAlarmsService;
	}

	@Override
	public Stream<NotificationBarElement> findAllCurrentUserNotifications() {
		return activeAlarmsService.findAllActiveAlarmsAssignToCurrentUser().stream()
			.map(activeAlarm ->
				new NotificationBarElement(
					getTranslation(
						"notifications.alarm.active",
						activeAlarm.projectAllocationName,
						activeAlarm.alarmName
					),
					ViewMode.PROJECT,
					ResourceAllocationsDetailsView.class,
					activeAlarm.projectId
				)
			);
	}
}
