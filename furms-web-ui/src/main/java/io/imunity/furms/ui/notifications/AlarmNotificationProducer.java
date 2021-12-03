/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;

import io.imunity.furms.api.alarms.FiredAlarmsService;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.project.allocations.ResourceAllocationsDetailsView;
import io.imunity.furms.ui.views.user_settings.projects.ProjectView;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

@Component
class AlarmNotificationProducer implements NotificationProducer{
	private final FiredAlarmsService firedAlarmsService;

	AlarmNotificationProducer(FiredAlarmsService firedAlarmsService) {
		this.firedAlarmsService = firedAlarmsService;
	}

	@Override
	public Stream<NotificationBarElement> findAllCurrentUserNotifications() {
		return firedAlarmsService.findAllFiredAlarmsOffCurrentUser().stream()
			.map(activeAlarm -> {
				String translation = getTranslation(
					"notifications.alarm.active",
					activeAlarm.alarm.projectAllocationName,
					activeAlarm.alarm.alarmName
				);
				NotificationBarElement.NotificationBarElementBuilder builder = NotificationBarElement.builder()
					.text(translation);

				if(activeAlarm.roles.contains(Role.PROJECT_ADMIN))
					builder
						.viewMode(ViewMode.PROJECT)
						.redirect(ResourceAllocationsDetailsView.class)
						.resourceId(activeAlarm.alarm.projectId)
						.parameter(activeAlarm.alarm.projectAllocationId);
				else if(activeAlarm.roles.contains(Role.PROJECT_USER))
					builder
						.viewMode(ViewMode.USER)
						.redirect(ProjectView.class)
						.parameter(activeAlarm.alarm.projectId);
				else
					builder
						.viewMode(ViewMode.USER);

				return builder.build();
			});
	}
}
