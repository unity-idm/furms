/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.notification;

import io.imunity.furms.domain.applications.ProjectApplicationEvent;
import io.imunity.furms.domain.notification.UserApplicationsListChangedEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.notifications.EmailNotificationDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class UserApplicationNotificationService {

	private final EmailNotificationDAO emailNotificationDAO;
	private final ApplicationEventPublisher publisher;

	UserApplicationNotificationService(EmailNotificationDAO emailNotificationDAO, ApplicationEventPublisher publisher) {
		this.emailNotificationDAO = emailNotificationDAO;
		this.publisher = publisher;
	}

	@EventListener
	void onProjectApplicationEvent(ProjectApplicationEvent event){
		UserApplicationsListChangedEvent notificationRequestEvent = event::isTargetedAt;
		publisher.publishEvent(notificationRequestEvent);
	}

	public void notifyUserAboutApplicationAcceptance(PersistentId id, String projectName) {
		emailNotificationDAO.notifyUserAboutApplicationAcceptance(id, projectName);
	}

	public void notifyUserAboutApplicationRejection(PersistentId id, String projectName) {
		emailNotificationDAO.notifyUserAboutApplicationRejection(id, projectName);
	}

	public void notifyAdminAboutApplicationRequest(PersistentId id, String projectId, String projectName, String applicationUserEmail) {
		emailNotificationDAO.notifyAdminAboutApplicationRequest(id, projectId, projectName, applicationUserEmail);
	}
}
