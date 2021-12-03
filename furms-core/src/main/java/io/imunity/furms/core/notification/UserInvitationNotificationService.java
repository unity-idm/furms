/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.notification;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.InvitationEvent;
import io.imunity.furms.domain.notification.UserInvitationsListChangedEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.notifications.EmailNotificationSender;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class UserInvitationNotificationService {

	private final EmailNotificationSender emailNotificationSender;
	private final ApplicationEventPublisher publisher;

	UserInvitationNotificationService(EmailNotificationSender emailNotificationSender, ApplicationEventPublisher publisher) {
		this.emailNotificationSender = emailNotificationSender;
		this.publisher = publisher;
	}

	@EventListener
	void onInvitationEvent(InvitationEvent event){
		publisher.publishEvent(new UserInvitationsListChangedEvent(event.getEmail()));
	}

	public void notifyUserAboutNewRole(PersistentId id, Role role) {
		emailNotificationSender.notifyUserAboutNewRole(id, role);
	}

	public void notifyAdminAboutRoleAcceptance(PersistentId id, Role role, String acceptanceUserEmail) {
		emailNotificationSender.notifyAdminAboutRoleAcceptance(id, role, acceptanceUserEmail);
	}

	public void notifyAdminAboutRoleRejection(PersistentId id, Role role, String rejectionUserEmail) {
		emailNotificationSender.notifyAdminAboutRoleRejection(id, role, rejectionUserEmail);
	}

}
