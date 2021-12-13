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
class ApplicationNotificationProducer implements NotificationProducer{
	private final ProjectApplicationsService projectApplicationsService;

	ApplicationNotificationProducer(ProjectApplicationsService projectApplicationsService) {
		this.projectApplicationsService = projectApplicationsService;
	}

	@Override
	public Stream<NotificationBarElement> findAllCurrentUserNotifications() {
		return projectApplicationsService.findAllApplicationsUsersForCurrentProjectAdmins().stream()
			.map(projectApplication ->
				NotificationBarElement.builder()
					.text(getTranslation(
						"notifications.new.application",
						projectApplication.user.email,
						projectApplication.projectName
					))
					.viewMode(ViewMode.PROJECT)
					.redirect(UsersView.class)
					.resourceId(projectApplication.projectId)
					.build()
				);
	}
}
