/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;

import io.imunity.furms.api.applications.ProjectApplicationsService;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.project.users.UsersView;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

@Component
class AlarmNotificationProducer implements NotificationProducer{
	private final ProjectApplicationsService projectApplicationsService;

	AlarmNotificationProducer(ProjectApplicationsService projectApplicationsService) {
		this.projectApplicationsService = projectApplicationsService;
	}

	@Override
	public Stream<NotificationBarElement> findAllCurrentUserNotifications() {
		return projectApplicationsService.findAllApplicationsUsersForCurrentProjectAdmins().stream()
			.map(projectApplication ->
				new NotificationBarElement(
					getTranslation(
						"notifications.new.application",
						projectApplication.user.email,
						projectApplication.projectName
					),
					ViewMode.PROJECT,
					UsersView.class,
					projectApplication.projectId
				)
			);
	}
}
