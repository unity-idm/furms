/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.notification;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationEvent;
import io.imunity.furms.domain.notification.UserInvitationsListChangedEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.notifications.EmailNotificationDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class UserInvitationNotificationService {

	private final EmailNotificationDAO emailNotificationDAO;
	private final ApplicationEventPublisher publisher;

	UserInvitationNotificationService(EmailNotificationDAO emailNotificationDAO, ApplicationEventPublisher publisher) {
		this.emailNotificationDAO = emailNotificationDAO;
		this.publisher = publisher;
	}

	@EventListener
	void onInvitationEvent(InvitationEvent event){
		publisher.publishEvent(new UserInvitationsListChangedEvent(event.getEmail()));
	}

	public void notifyUserAboutNewRole(PersistentId id, Role role) {
		emailNotificationDAO.notifyUserAboutNewRole(id, role);
	}

	public void notifyAdminAboutRoleAcceptance(PersistentId id, Role role, String acceptanceUserEmail) {
		emailNotificationDAO.notifyAdminAboutRoleAcceptance(id, role, acceptanceUserEmail);
	}

	public void notifyAdminAboutRoleRejection(PersistentId id, Role role, String rejectionUserEmail) {
		emailNotificationDAO.notifyAdminAboutRoleRejection(id, role, rejectionUserEmail);
	}

}
