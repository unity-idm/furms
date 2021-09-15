/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.notifications;

import io.imunity.furms.api.invitations.InviteeService;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.user_settings.invitations.InvitationsView;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

@Component
class InvitationNotificationProducer implements NotificationProducer{
	private final InviteeService inviteeService;

	InvitationNotificationProducer(InviteeService inviteeService) {
		this.inviteeService = inviteeService;
	}

	@Override
	public Stream<NotificationBarElement> findAllCurrentUserNotifications() {
		return inviteeService.findAllByCurrentUser().stream()
			.map(invitation ->
				new NotificationBarElement(
					getTranslation(
						"notifications.new.invitation",
						Optional.ofNullable(invitation.resourceName).map(name -> "'" + name + "'").orElse("") +
							" " +
							getTranslation("view.user-settings.invitations.grid.invitation.resource.type." + invitation.resourceId.type) +
							" " +
							getTranslation("view.user-settings.invitations.grid.invitation.role." + invitation.role.unityRoleValue)
					),
					ViewMode.USER,
					InvitationsView.class
				)
			);
	}
}
